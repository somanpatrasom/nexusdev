package com.nexusdev.controller;

import com.nexusdev.model.User;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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

    // POST /api/users/invite — generate invite code for new member
    @PostMapping("/invite")
    public ResponseEntity<String> generateInvite(@RequestBody User newUser) {
        String inviteCode = "NEXUS-" +
            newUser.getUsername().toUpperCase() + "-" +
            UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        newUser.setInviteCode(inviteCode);
        newUser.setRole("member");
        newUser.setJoinedAt(LocalDateTime.now());
        userRepository.save(newUser);

        return ResponseEntity.ok(
            "Invite created for " + newUser.getName() +
            "\nInvite code: " + inviteCode +
            "\nShare this: nexusdev join " + inviteCode
        );
    }

    // POST /api/users/join — join lab with invite code
    @PostMapping("/join")
    public ResponseEntity<String> joinLab(@RequestParam String inviteCode) {
        Optional<User> user = userRepository.findByInviteCode(inviteCode);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                "Welcome " + user.get().getName() + "!" +
                "\nYou have joined as: " + user.get().getRole() +
                "\nUsername: " + user.get().getUsername()
            );
        } else {
            return ResponseEntity.badRequest()
                .body("Invalid invite code: " + inviteCode);
        }
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