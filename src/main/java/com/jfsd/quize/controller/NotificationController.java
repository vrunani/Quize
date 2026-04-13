package com.jfsd.quize.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfsd.quize.entity.Notification;
import com.jfsd.quize.repository.NotificationRepository;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired private NotificationRepository notificationRepository;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Notification n) {
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        return ResponseEntity.ok("Sent");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> get(@PathVariable String userId) {
        return ResponseEntity.ok(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).get();
        n.setIsRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok("Read");
    }
}
