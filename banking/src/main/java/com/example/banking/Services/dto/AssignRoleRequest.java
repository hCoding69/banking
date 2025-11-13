package com.example.banking.Services.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssignRoleRequest {
    @NotNull(message = "This field is required")
    private Long userId;
    @NotNull(message = "This field is required")
    private Long roleId;

}
