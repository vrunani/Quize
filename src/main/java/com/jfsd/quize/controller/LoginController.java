package com.jfsd.quize.controller;

import com.jfsd.quize.dto.AddUserRequest;
import com.jfsd.quize.dto.LoginRequest;
import com.jfsd.quize.dto.LoginResponse;
import com.jfsd.quize.entity.User;
import com.jfsd.quize.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class LoginController {
        @GetMapping("/api/auth/users/count")
    public ResponseEntity<?> countStudents() {
        long count = userRepository.findAll().stream()
            .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()) && Boolean.TRUE.equals(u.getIsActive()))
            .count();
        return ResponseEntity.ok(Map.of("studentCount", count));
    }
    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // ADD USER
    // POST /api/auth/add-user
    // Body: { id, name, email, password, role }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/add-user")
    public ResponseEntity<String> addUser(@RequestBody AddUserRequest request) {

        if (request.getId() == null || request.getId().isBlank())
            return ResponseEntity.badRequest().body("User ID is required");

        if (userRepository.existsById(request.getId()))
            return ResponseEntity.badRequest().body("User ID already exists: " + request.getId());

        User user = new User();
        user.setId(request.getId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());   // ⚠ hash this with BCrypt in production
        user.setRole(request.getRole().toUpperCase());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return ResponseEntity.ok("User added successfully");
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // POST /api/auth/login
    // Body: { id, password, role }
    //
    // FIX #2: now returns LoginResponse JSON (id, name, email, role)
    // instead of a plain string so the frontend can store the
    // full user object in sessionStorage.
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. USER NOT FOUND
        Optional<User> optionalUser = userRepository.findById(request.getId());
        if (!optionalUser.isPresent())
            return ResponseEntity.badRequest().body("User ID not found");

        User user = optionalUser.get();

        // 2. WRONG PASSWORD
        if (!user.getPasswordHash().equals(request.getPassword()))
            return ResponseEntity.badRequest().body("Incorrect password");

        // 3. INACTIVE USER
        if (Boolean.FALSE.equals(user.getIsActive()))
            return ResponseEntity.badRequest().body("User account is inactive");

        // 4. ROLE MISMATCH
        if (request.getRole() == null ||
                !user.getRole().equalsIgnoreCase(request.getRole()))
            return ResponseEntity.status(403)
                    .body("You don't have access for role: " + request.getRole());

        // 5. SUCCESS — return full user object
        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful as " + user.getRole()
        );
        return ResponseEntity.ok(response);
    }
}