package com.example.banking.Services.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackRequest {

    @NotBlank(message = "This field is required")
    private String name;

    @NotBlank(message = "This field is required")
    private String description;

    @NotNull(message = "Monthly fee is required")
    @Min(value = 0, message = "Monthly fee must be >= 0")
    private double monthlyFee;

    @NotBlank(message = "This field is required")
    private String supportLevel;
    @NotNull(message = "Max transactions per day is required")
    @Min(value = 1, message = "Max transactions must be >= 1")
    private int maxTransactionsPerDay;

    @NotNull(message = "Insurance field is required")
    private boolean insurance;
}
