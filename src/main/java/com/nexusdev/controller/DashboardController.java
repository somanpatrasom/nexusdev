package com.nexusdev.controller;

import com.nexusdev.model.User;
import com.nexusdev.repository.DeviceRepository;
import com.nexusdev.repository.SessionRepository;
import com.nexusdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.nexusdev.repository.LabInstanceRepository;
import com.nexusdev.repository.LabTemplateRepository;
import com.nexusdev.service.LabTemplateService;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LabTemplateRepository labTemplateRepository;

    @Autowired
    private LabInstanceRepository labInstanceRepository;

    @Autowired
    private LabTemplateService labTemplateService;

    // Main dashboard — redirects based on role
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
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
        model.addAttribute("totalTemplates", labTemplateRepository.count());
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

    // Templates list page
    @GetMapping("/dashboard/templates")
    public String templatesPage(Model model) {
        model.addAttribute("templates", labTemplateRepository.findAll());
        model.addAttribute("totalTemplates", labTemplateRepository.count());
        model.addAttribute("totalInstances", labInstanceRepository.count());
        return "templates";
    }

    // Individual lab instance page
    @GetMapping("/dashboard/lab/{id}")
    public String labPage(@PathVariable Integer id, Model model) {
        labTemplateService.getInstanceById(id).ifPresent(instance -> {
            model.addAttribute("instance", instance);
            labTemplateService.getTemplateById(instance.getTemplateId())
                .ifPresent(template -> model.addAttribute("template", template));
        });
        return "lab";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
