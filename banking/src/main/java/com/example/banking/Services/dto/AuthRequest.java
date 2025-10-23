package com.example.banking.Services.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {

    @Email(message = "Email Not valid")
    @NotBlank(message = "The email field is required")
    private String email;

    @NotBlank(message = "The password field is required")
    @Size(min = 8, message = "The password must contains at least 8 characters")
    private String password;

    @Size(min = 6, max = 6, message = "The OTP field must must contains 6 characters")
    private String otp;
}
