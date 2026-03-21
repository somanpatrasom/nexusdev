package com.nexusdev.controller;

import com.nexusdev.model.Session;
import com.nexusdev.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionRepository sessionRepository;

    // GET /api/sessions — list all sessions
    @GetMapping
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    // GET /api/sessions/{id} — get one session
    @GetMapping("/{id}")
    public Session getSession(@PathVariable Integer id) {
        return sessionRepository.findById(id).orElse(null);
    }

    // POST /api/sessions — log a new session
    @PostMapping
    public Session createSession(@RequestBody Session session) {
        session.setCreatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // DELETE /api/sessions/{id} — delete a session
    @DeleteMapping("/{id}")
    public String deleteSession(@PathVariable Integer id) {
        sessionRepository.deleteById(id);
        return "Session " + id + " deleted";
    }
}