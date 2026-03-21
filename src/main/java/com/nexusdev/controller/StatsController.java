package com.nexusdev.controller;

import com.nexusdev.repository.SessionRepository;
import com.nexusdev.repository.DeviceRepository;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSessions", sessionRepository.count());
        stats.put("totalDevices", deviceRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", userRepository.countByRole("admin"));
        stats.put("totalMembers", userRepository.countByRole("member"));

        return stats;
    }
}