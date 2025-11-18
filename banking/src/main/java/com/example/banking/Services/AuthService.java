package com.example.banking.Services;

import com.example.banking.Models.Role;
import com.example.banking.Models.User;
import com.example.banking.Models.UserStatus;
import com.example.banking.Repositories.PackRepository;
import com.example.banking.Repositories.RoleRepository;
import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PackRepository packRepository;

    /**
     * Convertit un User en CustomUserDetails
     */
    private CustomUserDetails toCustomUserDetails(User user) {

        // Convertir les rôles en authorities Spring Security
        Collection<? extends GrantedAuthority> authorities =
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList();

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles(),   // Set<Role>
                authorities         // Collection<? extends GrantedAuthority>
        );
    }


    /**
     * Login
     */
    public ResponseEntity<AuthResponse> login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        CustomUserDetails userDetails = toCustomUserDetails(user);

        // Vérifier MFA
        if (user.getMfaSecret() != null) {
            if (request.getOtp() == null) {
                return ResponseEntity.ok(new AuthResponse(null, null, "MFA_REQUIRED"));
            }
            int otpCode = Integer.parseInt(request.getOtp());
            boolean otpValid = MFAService.verifyOtp(user.getMfaSecret(), otpCode);
            if (!otpValid) throw new RuntimeException("Invalid OTP");
        }

        // Générer JWT
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Crée les cookies HttpOnly
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60) // 24h
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(null, null, "Login successful"));
    }

    /**
     * Register
     */
    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already in use");

        String secret = MFAService.generateSecret();

        Set<Role> roles = request.getRoleIds().stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Role not found")))
                .collect(Collectors.toSet());

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBirthDate(request.getBirthDate());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserStatus(UserStatus.INACTIVE);
        user.setPack(packRepository.getById(request.getPackId()));
        user.setMfaSecret(secret);
        user.setRoles(roles);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLogin(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        String otpAuthUrl = MFAService.generateOtpAuthURL(user.getEmail(), secret);
        userRepository.save(user);

        return new RegisterResponse(secret, otpAuthUrl,
                "Registration successful. Please scan the QR code with Google Authenticator.");
    }

    /**
     * Refresh token
     */
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtService.extractTokenFromCookie(request, "refreshToken");
        if (refreshToken == null)
            return ResponseEntity.status(401).body(new AuthResponse(null, null, "Refresh token missing"));

        String email = jwtService.extractUsername(refreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails))
            return ResponseEntity.status(401).body(new AuthResponse(null, null, "Invalid refresh token"));

        String newAccessToken = jwtService.generateToken(userDetails);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(new AuthResponse(null, null, "Access token refreshed"));
    }

    /**
     * Logout
     */
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie clearAccessToken = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
                .build();
    }
}
