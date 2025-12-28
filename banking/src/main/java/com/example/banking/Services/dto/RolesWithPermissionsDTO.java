package com.example.banking.Services.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolesWithPermissionsDTO {

    private Long id;

    private String name;

    private String description;

    private Set<PermissionDTO> permissions;

}
