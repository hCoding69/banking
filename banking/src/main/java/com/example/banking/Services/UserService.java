package com.example.banking.Services;

import com.example.banking.Models.Role;
import com.example.banking.Models.User;
import com.example.banking.Repositories.RoleRepository;
import com.example.banking.Repositories.UserRepository;
import com.example.banking.Services.dto.PermissionDTO;
import com.example.banking.Services.dto.RolesWithPermissionsDTO;
import com.example.banking.Services.dto.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;


    public UserService(UserRepository userRepository, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // ici, roles sera accessible car transaction active
        UserResponse response = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserStatus()
        );

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> checkInternalAuth(String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            return ResponseEntity.badRequest().body("Missing Token");
        }

        String token = authHeader.substring(7);
        if(!jwtService.validateInternalToken(token)){
            return ResponseEntity.badRequest().body("Invalid Token");
        }
        return ResponseEntity.ok(true);
    }

    public ResponseEntity<?> getUserRolesWithPermissions(String token, Long id){

        ResponseEntity<?> authResponse = this.checkInternalAuth(token);

        if (authResponse.getStatusCode()!= HttpStatus.OK){
            return authResponse;
        }

        List<Role> roles = roleRepository.findByUsers_Id(id);

        List<RolesWithPermissionsDTO> dtoList = roles.stream()
                .map(role -> new RolesWithPermissionsDTO(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions()
                                .stream()
                                .map(p -> new PermissionDTO(p.getId(), p.getName(), p.getDescription()))
                                .collect(Collectors.toSet())
                )).toList();
        return ResponseEntity.ok(dtoList);

    }

    public ResponseEntity<?> assignRolesToUser(String token, Set<Long> roleReqIds, Long id) {

        ResponseEntity<?> authResponse = checkInternalAuth(token);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            return authResponse;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }

        Set<Role> foundRoles = new HashSet<>(roleRepository.findAllById(roleReqIds));

        if (foundRoles.size() != roleReqIds.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One or more roles not found");
        }

        Set<Role> userRoles = user.getRoles();

        Set<Role> rolesToAdd = foundRoles.stream()
                .filter(role -> !userRoles.contains(role))
                .collect(Collectors.toSet());

        if (rolesToAdd.size() != roleReqIds.size()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Some Roles are Already Assigned to this user");
        }


        userRoles.addAll(rolesToAdd);
        userRepository.save(user);

        return ResponseEntity.ok("Roles assigned successfully");
    }

    public ResponseEntity<?> RemoveRolesFromUser(String token, Set<Long> roleReqIds, Long id) {

        ResponseEntity<?> authResponse = checkInternalAuth(token);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            return authResponse;
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        }

        Set<Role> foundRoles = new HashSet<>(roleRepository.findAllById(roleReqIds));

        if (foundRoles.size() != roleReqIds.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One or more roles not found");
        }

        Set<Role> userRoles = user.getRoles();

        Set<Role> rolesToRemove = foundRoles.stream()
                .filter(userRoles::contains)
                .collect(Collectors.toSet());

        if (rolesToRemove.size() != roleReqIds.size()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Some Roles are Not Assigned to this user");
        }


        userRoles.removeAll(rolesToRemove);
        userRepository.save(user);

        return ResponseEntity.ok("Roles assigned successfully");
    }



}
