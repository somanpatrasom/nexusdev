package com.nexusdev.controller;

import com.nexusdev.model.User;
import com.nexusdev.repository.DeviceRepository;
import com.nexusdev.repository.SessionRepository;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    // Main dashboard — redirects based on role
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    // Dashboard page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("sessions", sessionRepository.findAll());
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalSessions", sessionRepository.count());
        model.addAttribute("totalDevices", deviceRepository.count());
        model.addAttribute("totalAdmins", userRepository.countByRole("admin"));
        model.addAttribute("totalMembers", userRepository.countByRole("member"));
        return "dashboard";
    }

    // Invite page
    @GetMapping("/invite")
    public String invitePage(Model model) {
        model.addAttribute("user", new User());
        return "invite";
    }

    // Join page
    @GetMapping("/join")
    public String joinPage() {
        return "join";
    }
}
