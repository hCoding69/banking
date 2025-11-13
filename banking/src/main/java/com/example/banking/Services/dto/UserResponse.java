package com.example.banking.Services.dto;

import com.example.banking.Models.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserStatus userStatus;

}
