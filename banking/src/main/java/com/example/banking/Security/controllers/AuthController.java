package com.example.banking.Security.controllers;

import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.AuthService;
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
import org.springframework.http.ResponseEntity;
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


    private final JwtService jwtService;
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){
        return authService.login(request);
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
