package com.jfsd.quize;

import com.jfsd.quize.entity.User;
import com.jfsd.quize.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {

        // Demo Teacher
        if (!userRepository.existsById("teacher01")) {
            User teacher = new User();
            teacher.setId("teacher01");
            teacher.setName("Demo Teacher");
            teacher.setEmail("teacher@demo.com");
            teacher.setPasswordHash("teacher123");
            teacher.setRole("TEACHER");
            teacher.setIsActive(true);
            teacher.setCreatedAt(LocalDateTime.now());
            userRepository.save(teacher);
            System.out.println("Demo teacher created");
        }

        // Demo Student
        if (!userRepository.existsById("student01")) {
            User student = new User();
            student.setId("student01");
            student.setName("Demo Student");
            student.setEmail("student@demo.com");
            student.setPasswordHash("student123");
            student.setRole("STUDENT");
            student.setIsActive(true);
            student.setCreatedAt(LocalDateTime.now());
            userRepository.save(student);
            System.out.println("Demo student created");
        }
    }
}
