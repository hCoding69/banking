package com.example.banking.Models;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(
                    name = "role_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id"
            )
    )
    private Set<User> users = new HashSet<>();



    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(
                    name = "role_id"
            ),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Set<Permission> getPermissions() {
        return this.permissions;
    }



}
