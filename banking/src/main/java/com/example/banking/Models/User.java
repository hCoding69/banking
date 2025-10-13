package com.example.banking.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private LocalDateTime lastLoginAt;
    private double failedLogin;
    private String mfaSecret;
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @ManyToMany(mappedBy = "users")
    public Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "user")
    public Set<AuditLog> auditLogs = new HashSet<>();

    @OneToMany(mappedBy = "user")
    public Set<Token> tokens = new HashSet<>();

}
