package com.example.banking.Services.dto;

import com.example.banking.Models.Role;
import com.example.banking.Models.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Data
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Set<Role> roles;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String email, String password, Set<Role> roles, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.authorities = authorities;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() {
        return this.roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}

