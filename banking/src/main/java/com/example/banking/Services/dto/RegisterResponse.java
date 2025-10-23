package com.example.banking.Services.dto;


import lombok.Data;

@Data
public class RegisterResponse {
    private String mfaSecret;
    private String qrCodeUrl;
    private String message;
}
