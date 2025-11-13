package com.example.banking.Repositories;

import com.example.banking.Models.User;
import com.example.banking.Services.dto.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByEmail(String email);

    public boolean existsByEmail(String email);

    public Optional<User> findByEmailAndUserStatus(String email, String userStatus);

}
