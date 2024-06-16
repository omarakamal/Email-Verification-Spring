package com.example.auth.repository;


import com.example.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);

//    method used to get the user based on the token
    AppUser findByVerificationToken(String token);
}