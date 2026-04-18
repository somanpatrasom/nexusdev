package com.nexusdev.controller;

import com.nexusdev.model.User;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /api/users — list all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET /api/users/{id} — get one user
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        return userRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users/invite — admin generates invite code
    // Body: { "name": "Alice", "username": "alice", "role": "member" }
    @PostMapping("/invite")
    public ResponseEntity<String> generateInvite(@RequestBody User newUser) {
        // Check username not already taken
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                .body("Username already exists: " + newUser.getUsername());
        }

        String inviteCode = "NEXUS-" +
            newUser.getUsername().toUpperCase() + "-" +
            UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        newUser.setInviteCode(inviteCode);
        newUser.setRole(newUser.getRole() != null ? newUser.getRole() : "member");
        newUser.setJoinedAt(LocalDateTime.now());
        // Temporary password — user will set their own on join
        newUser.setPassword(passwordEncoder.encode("changeme"));
        userRepository.save(newUser);

        return ResponseEntity.ok(
            "Invite created for " + newUser.getName() +
            "\nInvite code: " + inviteCode
        );
    }

    // POST /api/users/join — join with invite code + set your password
    // Body: { "inviteCode": "NEXUS-ALICE-3F78", "password": "mypassword123" }
    @PostMapping("/join")
    public ResponseEntity<String> joinWithCode(@RequestBody Map<String, String> body) {
        String inviteCode = body.get("inviteCode");
        String rawPassword = body.get("password");

        if (inviteCode == null || rawPassword == null) {
            return ResponseEntity.badRequest()
                .body("inviteCode and password are required");
        }

        Optional<User> userOpt = userRepository.findByInviteCode(inviteCode);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Invalid invite code: " + inviteCode);
        }

        User user = userOpt.get();

        // Set their chosen password (BCrypt hashed)
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setJoinedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(
            "Welcome " + user.getName() + "!" +
            "\nUsername: " + user.getUsername() +
            "\nRole: " + user.getRole() +
            "\nYou can now log in at /login"
        );
    }

    // DELETE /api/users/{id} — remove a user
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User " + id + " removed");
        }
        return ResponseEntity.notFound().build();
    }
}