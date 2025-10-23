package com.example.banking.Services.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {


    private String mfaSecret;
    private String qrCodeUrl;
    private String message;
}
