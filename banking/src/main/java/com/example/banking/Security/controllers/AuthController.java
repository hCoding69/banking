package com.example.banking.Security.controllers;

import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.JwtService;
import com.example.banking.Services.MFAService;
import com.example.banking.Services.UserDetailsServiceImpl;
import com.example.banking.Services.dto.AuthRequest;
import com.example.banking.Services.dto.AuthResponse;
import com.example.banking.Models.User;
import com.example.banking.Services.dto.RegisterRequest;
import com.example.banking.Services.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final UserRepository  userRepository;


    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request){

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e){
            throw new RuntimeException("Invalid email or password");
        }
        User user = (User) userDetailsService.loadUserByUsername(request.getEmail());

        if(user.getMfaSecret() != null){
            if(request.getOtp() == null){
                return new AuthResponse(null, null, "MFA_REQUIRED");
            }
            int otpCode = Integer.parseInt(request.getOtp());

            boolean otpValid = MFAService.verifyOtp(user.getMfaSecret(), otpCode);

            if(!otpValid){
                throw new RuntimeException("Invalid OTP");
            }
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, "Login Successful");
    }

    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        String secret = MFAService.generateSecret();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setBirthDate(request.getBirthDate());
        user.setMfaSecret(secret);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(userDetailsService.encodePassword(request.getPassword()));

        userDetailsService.saveUser(user);


    }
}
