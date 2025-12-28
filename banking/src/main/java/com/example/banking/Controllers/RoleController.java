package com.example.banking.Controllers;


import com.example.banking.Services.RoleService;
import com.example.banking.Services.dto.RoleDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService){
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<?> getRoles(
            @RequestHeader("Authorization") String authHeader
    ){
        return roleService.getRoles(authHeader);
    }

    @GetMapping("/with-permissions/{id}")
    public ResponseEntity<?> getRolesWithPermissions(@RequestHeader("Authorization") String authHeader,
                                                     @PathVariable(required = false) Long id){
        return roleService.getRolesWithPermissions(authHeader, id);
    }


    @PostMapping
    public ResponseEntity<?> createRole(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody RoleDTO req){
        return roleService.createRole(authHeader, req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable Long id, @Valid @RequestBody RoleDTO req
                                        ){
        return roleService.updateRole(authHeader, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@RequestHeader("Authorization") String authHeader,
                                        @PathVariable Long id){
        return roleService.deleteRole(authHeader, id);
    }
}
