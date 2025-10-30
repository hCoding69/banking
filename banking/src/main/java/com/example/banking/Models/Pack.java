package com.example.banking.Models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Pack {


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double monthlyFee;
    private String supportLevel;
    private int maxTransactionsPerDay;
    private boolean insurance;

    @OneToMany(mappedBy = "pack")
    private List<User> users;

}
