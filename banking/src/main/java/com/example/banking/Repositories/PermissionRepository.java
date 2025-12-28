package com.example.banking.Repositories;

import com.example.banking.Models.Permission;
import com.example.banking.Services.dto.PermissionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {



    @Query("Select new com.example.banking.Services.dto.PermissionDTO(p.id, p.name, p.description)" +
            "From Permission p Order By name")
    List<PermissionDTO> findAllOrderByName();
}
