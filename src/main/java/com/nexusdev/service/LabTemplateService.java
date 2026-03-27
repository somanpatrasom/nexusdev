package com.nexusdev.service;

import com.nexusdev.model.LabInstance;
import com.nexusdev.model.LabTemplate;
import com.nexusdev.repository.LabInstanceRepository;
import com.nexusdev.repository.LabTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LabTemplateService {

    @Autowired
    private LabTemplateRepository templateRepository;

    @Autowired
    private LabInstanceRepository instanceRepository;

    // Professor creates a template
    public LabTemplate createTemplate(LabTemplate template) {
        // Generate a short share code: LAB-XXXXXX
        String code = "LAB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        template.setShareCode(code);
        template.setCreatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    // List all templates
    public List<LabTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    // Get one template by ID
    public Optional<LabTemplate> getTemplateById(Integer id) {
        return templateRepository.findById(id);
    }

    // Get template by share code (what students use)
    public Optional<LabTemplate> getTemplateByCode(String code) {
        return templateRepository.findByShareCode(code);
    }

    // Student spawns a lab instance from a share code
    public LabInstance spawnInstance(String shareCode, Integer userId) {
        Optional<LabTemplate> templateOpt = templateRepository.findByShareCode(shareCode);

        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + shareCode);
        }

        LabTemplate template = templateOpt.get();

        LabInstance instance = new LabInstance();
        instance.setTemplateId(template.getId());
        instance.setUserId(userId);
        instance.setStatus("active");
        // Copy the notes scaffold from template as starting notes
        instance.setNotes(template.getNotesScaffold());
        instance.setStartedAt(LocalDateTime.now());

        return instanceRepository.save(instance);
    }

    // List all instances for a user
    public List<LabInstance> getInstancesByUser(Integer userId) {
        return instanceRepository.findByUserId(userId);
    }

    // Get one instance
    public Optional<LabInstance> getInstanceById(Integer id) {
        return instanceRepository.findById(id);
    }

    // Mark instance complete
    public LabInstance completeInstance(Integer instanceId) {
        LabInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException("Instance not found: " + instanceId));
        instance.setStatus("completed");
        instance.setCompletedAt(LocalDateTime.now());
        return instanceRepository.save(instance);
    }
}
