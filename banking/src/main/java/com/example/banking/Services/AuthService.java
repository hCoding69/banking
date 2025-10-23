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
import lombok.RequiredArgsConstructor;
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

    public AuthResponse login(AuthRequest request) {

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
                return new AuthResponse(null, null, "MFA_REQUIRED");
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

        return new AuthResponse(accessToken, refreshToken, "Login Successful");
    }

    public User register(RegisterRequest request) {

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

        return user;
    }

}
