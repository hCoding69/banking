package com.example.banking.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String password;
    private LocalDateTime lastLoginAt;
    private double failedLogin;
    private String mfaSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    public Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "user")

    public Set<AuditLog> auditLogs = new HashSet<>();

    @OneToMany(mappedBy = "user")
    public Set<Token> tokens = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream()) // 2️⃣ Pour chaque rôle, récupère toutes ses permissions et les met dans un seul flux
                .map(permission -> new SimpleGrantedAuthority(permission.getName())) // 3️⃣ Transforme chaque Permission en GrantedAuthority
                .collect(Collectors.toSet()); // 4️⃣ Collecte le tout dans un Set (pour éviter les doublons)
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.userStatus.equals(UserStatus.SUSPENDED) && !this.userStatus.equals(UserStatus.BLOCKED)  ;
    }
    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.userStatus.equals(UserStatus.ACTIVE) || this.userStatus.equals(UserStatus.INACTIVE);
    }

    @ManyToOne
    @JoinColumn(name = "pack_id")
    private Pack pack;
}