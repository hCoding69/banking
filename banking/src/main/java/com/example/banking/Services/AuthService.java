package com.example.banking.Services;

import com.example.banking.Models.Role;
import com.example.banking.Models.User;
import com.example.banking.Models.UserStatus;
import com.example.banking.Repositories.RoleRepository;
import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.dto.AuthRequest;
import com.example.banking.Services.dto.AuthResponse;
import com.example.banking.Services.dto.RegisterRequest;
import com.example.banking.Services.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
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



    public ResponseEntity<AuthResponse> login(AuthRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = (User) userDetailsService.loadUserByUsername(request.getEmail());

        // Vérifier OTP (MFA)
        if (user.getMfaSecret() != null) {
            if (request.getOtp() == null) {
                return ResponseEntity.ok(new AuthResponse(null, null, "MFA_REQUIRED"));
            }
            int otpCode = Integer.parseInt(request.getOtp());
            boolean otpValid = MFAService.verifyOtp(user.getMfaSecret(), otpCode);

            if (!otpValid) {
                throw new RuntimeException("Invalid OTP");
            }
        }

        // Génération JWT
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Crée le cookie HttpOnly
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)  // 24h
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(null, null, "Login Successful"));
    }


    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

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
        user.setMfaSecret(secret);
        user.setRoles(roles);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLogin(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        String otpAuthUrl = MFAService.generateOtpAuthURL(user.getEmail(), secret);
        userRepository.save(user);

        return new RegisterResponse(secret, otpAuthUrl, "Registration successful. Please scan the QR code with Google Authenticator.");
    }
    // ==================== REFRESH TOKEN ====================
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {

        String refreshToken = jwtService.extractTokenFromCookie(request, "refreshToken");

        String userEmail = jwtService.extractUsername(refreshToken);
        User user = (User) userDetailsService.loadUserByUsername(userEmail);

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken, user)) {
            return ResponseEntity.status(401).body(new AuthResponse(null, null, "Invalid refresh token"));
        }


        String newAccessToken = jwtService.generateToken(user);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(new AuthResponse(null, null, "Access token refreshed"));
    }

    // ==================== LOGOUT ====================
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie clearAccessToken = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
                .build();
    }
}

