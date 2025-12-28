package com.example.banking.Repositories;


import com.example.banking.Models.Role;
import com.example.banking.Services.dto.RoleDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    @Query("Select new com.example.banking.Services.dto.RoleDTO(r.id, r.name, r.description)" +
            "From Role r Order By name")
    List<RoleDTO> findAllRolesOrderByName();


    boolean existsByName(String name);

    List<Role> findByUsers_Id(Long id);

}
