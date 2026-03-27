package com.nexusdev.controller;

import com.nexusdev.model.LabInstance;
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

    // GET /api/templates — list all templates
    @GetMapping
    public List<LabTemplate> getAllTemplates() {
        return labTemplateService.getAllTemplates();
    }

    // GET /api/templates/{id} — get one template
    @GetMapping("/{id}")
    public LabTemplate getTemplate(@PathVariable Integer id) {
        return labTemplateService.getTemplateById(id).orElse(null);
    }

    // GET /api/templates/code/{shareCode} — get by share code
    @GetMapping("/code/{shareCode}")
    public LabTemplate getTemplateByCode(@PathVariable String shareCode) {
        return labTemplateService.getTemplateByCode(shareCode).orElse(null);
    }

    // POST /api/templates — professor creates a template
    @PostMapping
    public LabTemplate createTemplate(@RequestBody LabTemplate template) {
        return labTemplateService.createTemplate(template);
    }

    // POST /api/templates/spawn — student spawns a lab
    // Body: { "shareCode": "LAB-ABC123", "userId": 1 }
    @PostMapping("/spawn")
    public LabInstance spawnLab(@RequestBody Map<String, Object> body) {
        String shareCode = (String) body.get("shareCode");
        Integer userId = (Integer) body.get("userId");
        return labTemplateService.spawnInstance(shareCode, userId);
    }

    // GET /api/templates/instances/user/{userId} — list user's labs
    @GetMapping("/instances/user/{userId}")
    public List<LabInstance> getUserInstances(@PathVariable Integer userId) {
        return labTemplateService.getInstancesByUser(userId);
    }

    // GET /api/templates/instances/{id} — get one instance
    @GetMapping("/instances/{id}")
    public LabInstance getInstance(@PathVariable Integer id) {
        return labTemplateService.getInstanceById(id).orElse(null);
    }

    // POST /api/templates/instances/{id}/complete — mark lab done
    @PostMapping("/instances/{id}/complete")
    public LabInstance completeInstance(@PathVariable Integer id) {
        return labTemplateService.completeInstance(id);
    }
}