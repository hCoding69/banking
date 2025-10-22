package com.example.banking.Security.controllers;

import com.example.banking.Services.JwtService;
import com.example.banking.Services.MFAService;
import com.example.banking.Services.UserDetailsServiceImpl;
import com.example.banking.Services.dto.AuthRequest;
import com.example.banking.Services.dto.AuthResponse;
import com.example.banking.Models.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private AuthenticationManager authentificationManager;
    private UserDetailsServiceImpl userDetailsService;
    private MFAService mfaService;
    private JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request){

        try{
            authentificationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e){
            throw new RuntimeException("Invalid email or password");
        }
        // Charger lutilisateur depuis la base
        User user = (User) userDetailsService.loadUserByUsername(request.getEmail());

        if(user.getMfaSecret() != null){
            if(request.getOtp() == null){
                return new AuthResponse(null, null, "MFA required");
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
}
