package com.jfsd.quize.controller;

import com.jfsd.quize.dto.AddUserRequest;
import com.jfsd.quize.dto.LoginRequest;
import com.jfsd.quize.entity.User;
import com.jfsd.quize.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    // ADD USER
    @PostMapping("/add-user")
    public ResponseEntity<String> addUser(@RequestBody AddUserRequest request) {

        User user = new User();
        user.setId(request.getId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok("User added successfully");
    }
@PostMapping("/login")
public ResponseEntity<String> login(@RequestBody LoginRequest request) {

    Optional<User> optionalUser = userRepository.findById(request.getId());

    // ❌ 1. USER NOT FOUND
    if (!optionalUser.isPresent()) {
        return ResponseEntity.badRequest().body("User ID not found");
    }

    User user = optionalUser.get();

    // ❌ 2. WRONG PASSWORD
    if (!user.getPasswordHash().equals(request.getPassword())) {
        return ResponseEntity.badRequest().body("Incorrect password");
    }

    // ❌ 3. INACTIVE USER
    if (!user.getIsActive()) {
        return ResponseEntity.badRequest().body("User account inactive");
    }

    // ❌ 4. ROLE MISMATCH
    if (request.getRole() == null || 
    !user.getRole().toString().equalsIgnoreCase(request.getRole())) {
    return ResponseEntity.status(403).body("You don't have access for this role");
}

    // ✅ SUCCESS
    return ResponseEntity.ok("Login successful as " + user.getRole());
}
}