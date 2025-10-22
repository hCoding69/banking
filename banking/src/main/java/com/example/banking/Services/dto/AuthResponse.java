package com.example.banking.Services.dto;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthResponse {

    private String refreshToken;
    private String accessToken;
    private String message;
}
