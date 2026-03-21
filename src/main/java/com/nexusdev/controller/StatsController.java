package com.nexusdev.controller;

import com.nexusdev.repository.SessionRepository;
import com.nexusdev.repository.DeviceRepository;
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

    @GetMapping
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalSessions = sessionRepository.count();
        stats.put("totalSessions", totalSessions);
        stats.put("totalDevices", deviceRepository.count());

        return stats;
    }

}
