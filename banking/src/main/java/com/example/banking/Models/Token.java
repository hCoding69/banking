package com.example.banking.Models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Token {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private String type;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
