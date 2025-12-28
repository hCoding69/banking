package com.example.banking.Services.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class PermissionDTO {
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    public PermissionDTO(Long id,String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

}
