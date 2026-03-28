package com.nexusdev.controller;

import com.nexusdev.model.LabInstance;
import com.nexusdev.model.LabMember;
import com.nexusdev.model.LabTemplate;
import com.nexusdev.service.LabTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class LabTemplateController {

    @Autowired
    private LabTemplateService labTemplateService;

    // GET /api/templates
    @GetMapping
    public List<LabTemplate> getAllTemplates() {
        return labTemplateService.getAllTemplates();
    }

    // GET /api/templates/{id}
    @GetMapping("/{id}")
    public LabTemplate getTemplate(@PathVariable Integer id) {
        return labTemplateService.getTemplateById(id).orElse(null);
    }

    // GET /api/templates/code/{shareCode}
    @GetMapping("/code/{shareCode}")
    public LabTemplate getTemplateByCode(@PathVariable String shareCode) {
        return labTemplateService.getTemplateByCode(shareCode).orElse(null);
    }

    // POST /api/templates
    @PostMapping
    public LabTemplate createTemplate(@RequestBody LabTemplate template) {
        return labTemplateService.createTemplate(template);
    }

    // POST /api/templates/spawn
    // Body: { "shareCode": "LAB-ABC123", "userId": 1 }
    @PostMapping("/spawn")
    public LabInstance spawnLab(@RequestBody Map<String, Object> body) {
        String shareCode = (String) body.get("shareCode");
        Integer userId   = (Integer) body.get("userId");
        return labTemplateService.spawnInstance(shareCode, userId);
    }

    // GET /api/templates/instances/user/{userId}
    @GetMapping("/instances/user/{userId}")
    public List<LabInstance> getUserInstances(@PathVariable Integer userId) {
        return labTemplateService.getInstancesByUser(userId);
    }

    // GET /api/templates/instances/{id}
    @GetMapping("/instances/{id}")
    public LabInstance getInstance(@PathVariable Integer id) {
        return labTemplateService.getInstanceById(id).orElse(null);
    }

    // POST /api/templates/instances/{id}/complete
    @PostMapping("/instances/{id}/complete")
    public LabInstance completeInstance(@PathVariable Integer id) {
        return labTemplateService.completeInstance(id);
    }

    // POST /api/templates/instances/{id}/members
    // Body: { "userId": 2, "username": "alice", "password": "pass123" }
    @PostMapping("/instances/{id}/members")
    public LabMember addMember(@PathVariable Integer id,
                               @RequestBody Map<String, Object> body) {
        Integer userId  = (Integer) body.get("userId");
        String username = (String)  body.get("username");
        String password = (String)  body.get("password");
        return labTemplateService.addMemberToLab(id, userId, username, password);
    }

    // DELETE /api/templates/instances/{id}/members/{username}
    @DeleteMapping("/instances/{id}/members/{username}")
    public String removeMember(@PathVariable Integer id,
                               @PathVariable String username) {
        labTemplateService.removeMemberFromLab(id, username);
        return "Member " + username + " removed from lab " + id;
    }

    // GET /api/templates/instances/{id}/members
    @GetMapping("/instances/{id}/members")
    public List<LabMember> getMembers(@PathVariable Integer id) {
        return labTemplateService.getLabMembers(id);
    }

    // POST /api/templates/instances/{id}/stop
    @PostMapping("/instances/{id}/stop")
    public LabInstance stopLab(@PathVariable Integer id) {
        return labTemplateService.stopLab(id);
    }
}