package com.example.jwo.controller;

import com.example.jwo.entity.Kala;
import com.example.jwo.entity.User;
import com.example.jwo.repository.KalaRepository;
import com.example.jwo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final KalaRepository kalaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, KalaRepository kalaRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.kalaRepository = kalaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("Returning users: " + users.size());
        return users;
    }

    @GetMapping("/kalat")
    public List<Kala> getAllKalat() {
        List<Kala> kalat = kalaRepository.findAll();
        System.out.println("Returning kalat: " + kalat.size());
        return kalat;
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null || user.getRole() == null) {
            System.out.println("Create user failed: Missing fields");
            return ResponseEntity.badRequest().build();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        System.out.println("Created user: " + user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Update user failed: ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
        User updated = existing.get();
        updated.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            updated.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        updated.setRole(user.getRole());
        userRepository.save(updated);
        System.out.println("Updated user: " + updated.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            System.out.println("Delete user failed: ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        System.out.println("Deleted user ID: " + id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kalat")
    @Transactional
    public ResponseEntity<Void> createKala(@RequestBody Kala kala) {
        if (kala.getLaji() == null || kala.getPituus() == null || kala.getPaino() == null) {
            System.out.println("Create kala failed: Missing fields");
            return ResponseEntity.badRequest().build();
        }
        kalaRepository.save(kala);
        System.out.println("Created kala: " + kala.getLaji());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/kalat/{id}")
    @Transactional
    public ResponseEntity<Void> updateKala(@PathVariable Long id, @RequestBody Kala kala) {
        Optional<Kala> existing = kalaRepository.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Update kala failed: ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
        Kala updated = existing.get();
        updated.setLaji(kala.getLaji());
        updated.setPituus(kala.getPituus());
        updated.setPaino(kala.getPaino());
        updated.setUser(kala.getUser());
        kalaRepository.save(updated);
        System.out.println("Updated kala: " + updated.getLaji());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/kalat/{id}")
    @Transactional
    public ResponseEntity<Void> deleteKala(@PathVariable Long id) {
        if (!kalaRepository.existsById(id)) {
            System.out.println("Delete kala failed: ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
        kalaRepository.deleteById(id);
        System.out.println("Deleted kala ID: " + id);
        return ResponseEntity.ok().build();
    }
}