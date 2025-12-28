package com.example.banking.Controllers;


import com.example.banking.Services.PermissionService;
import com.example.banking.Services.dto.PermissionDTO;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService){
        this.permissionService = permissionService;
    }

    @GetMapping()
    public ResponseEntity<?> getPermissions(@RequestHeader("Authorization") String authHeader){
        return this.permissionService.getPermissions(authHeader);
    }

    @PostMapping()
    public ResponseEntity<?> createPermission(@RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody PermissionDTO request
                                              ){
        return this.permissionService.createPermission(authHeader, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editPermission(@RequestHeader("Authorization") String authHeader,
                                            @Valid @RequestBody PermissionDTO request,
                                            @PathVariable Long id
                                            ){
        return this.permissionService.updatePermission(id, request, authHeader);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long id
    ){
        return this.permissionService.deletePermission(id, authHeader);
    }


}


