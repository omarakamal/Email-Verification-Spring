package com.example.auth.controller;


import com.example.auth.entity.AppUser;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AppUser user) {
        if (userService.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }  try {
            userService.saveUser(user);
            return ResponseEntity.ok("User registered successfully. Check your email for verification.");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Failed to send verification email");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody AppUser user) {
        AppUser existingUser = userService.findByEmail(user.getEmail());
        if (existingUser == null || !bCryptPasswordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
        if(existingUser.isEnabled() == false){
            System.out.println("USER NOT ENABLED");
            return ResponseEntity.badRequest().body("Please check your email for verification link");

        }
        return ResponseEntity.ok("User logged in successfully");
    }

    @GetMapping("/verify")
    public void verifyUser(@RequestParam String token) {
        AppUser user = userRepository.findByVerificationToken(token);
        if (user != null) {
            user.setEnabled(true); // Set enabled to true
            userRepository.save(user); // Save updated user
        } else {
            throw new IllegalArgumentException("Invalid verification token");
        }
    }
}