package com.example.banking.Repositories;
import com.example.banking.Models.User;
import com.example.banking.Services.dto.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
public interface ResponseRepository extends JpaRepository<User, Long>
{
    @Query("SELECT new com.example.banking.Services.dto.UserResponse(u.id, u.firstName, u.lastName, u.email, u.userStatus) " + "FROM User u WHERE u.email = :email")
    Optional<UserResponse> findUserResponseByEmail(String email);

}