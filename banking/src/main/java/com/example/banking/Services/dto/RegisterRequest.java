package com.example.banking.Services.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private LocalDate birthDate;
    private List<Long> roleIds;

}
