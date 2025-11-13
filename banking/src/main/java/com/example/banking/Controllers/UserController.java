package com.example.banking.Controllers;

import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.*;
import com.example.banking.Services.dto.*;
import com.example.banking.Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;




    @RestController
    @RequestMapping("/api/users")
    public class UserController {

        private RoleService roleService;
        private UserService userService;

        public UserController(RoleService roleService, UserService userService){
            this.roleService = roleService;
            this.userService = userService;
        }

        @PostMapping("/roles/assign-role")
        public ResponseEntity<?> assignRole(@Valid @RequestBody AssignRoleRequest request){
            return roleService.assignRole(request);
        }

        @PostMapping("/roles/create")

        public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request){
            return roleService.createRole(request);
        }

        @GetMapping("/me")
        public ResponseEntity<UserResponse> me(Authentication authentication){
            return userService.getCurrentUser(authentication);
        }

    }


