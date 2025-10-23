package com.example.banking.Services.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String refreshToken;
    private String accessToken;
    private String message;
}
