package com.example.banking.Services.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "This Field Name is Required")
    private String firstName;

    @NotBlank(message = "This Field Name is Required")
    private String lastName;

    @Email(message = "The email field is required")
    private String email;

    @NotBlank(message = "This Field Name is Required")
    private String password;

    @NotBlank(message = "This Field Name is Required")
    private String confirmPassword;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @NotNull(message = "Birth date is required")
    private List<Long> roleIds;

}
