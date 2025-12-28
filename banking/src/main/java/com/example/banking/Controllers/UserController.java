package com.example.banking.Controllers;

import com.example.banking.Services.*;
import com.example.banking.Services.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
    @RequestMapping("/api/users")
    public class UserController {

        private final UserService userService;

        public UserController(RoleService roleService, UserService userService){
            this.userService = userService;
        }


        @GetMapping("/me")
        public ResponseEntity<UserResponse> me(Authentication authentication){
            return userService.getCurrentUser(authentication);
        }

        @GetMapping("/{id}/roles")
        public ResponseEntity<?> getUserRolesWithPermissions(@RequestHeader("Authorization") String token,
                                                             @PathVariable Long id){
            return userService.getUserRolesWithPermissions(token, id);

        }

        @PostMapping("/{id}/roles")
        public ResponseEntity<?> assignRolesToUser(@RequestHeader("Authorization") String token,
                                                   @RequestBody Set<Long> roleReqIds,
                                                   @PathVariable Long id){
            return userService.assignRolesToUser(token,roleReqIds, id);

        }

        @DeleteMapping("/{id}/roles")
        public ResponseEntity<?> RemoveRolesFromUser(@RequestHeader("Authorization") String token,
                                                     @RequestBody Set<Long> roleReqIds,
                                                     @PathVariable Long id){
            return userService.RemoveRolesFromUser(token,roleReqIds, id);

        }

    }


