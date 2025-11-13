package com.example.banking.Services;

import com.example.banking.Models.Role;
import com.example.banking.Models.User;
import com.example.banking.Repositories.RoleRepository;
import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.dto.AssignRoleRequest;
import com.example.banking.Services.dto.CreateRoleRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<?> assignRole(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (user.getRoles().contains(role)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "User already has role '" + role.getName() + "'")
            );
        }

        user.getRoles().add(role);
        userRepository.save(user);

        Map<String, Object> response = Map.of(
                "message", "Role '" + role.getName() + "' assigned to user '" + user.getEmail() + "'",
                "userId", user.getId(),
                "roleId", role.getId()
        );

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> createRole(CreateRoleRequest request) {
        roleRepository.findByName(request.getName()).ifPresent(r -> {
            throw new RuntimeException("Role '" + request.getName() + "' already exists");
        });

        Role role = new Role();
        role.setName(request.getName());
        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body("Role '" + request.getName() + "' created successfully");
    }
}
