package com.nexusdev.service;

import com.nexusdev.model.LabInstance;
import com.nexusdev.model.LabMember;
import com.nexusdev.model.LabTemplate;
import com.nexusdev.repository.LabInstanceRepository;
import com.nexusdev.repository.LabMemberRepository;
import com.nexusdev.repository.LabTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LabTemplateService {

    @Autowired
    private LabTemplateRepository templateRepository;

    @Autowired
    private LabInstanceRepository instanceRepository;

    @Autowired
    private LabMemberRepository memberRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private DockerService dockerService;

    private static final String LAB_BASE_PATH = "/opt/nexusdev/lab/members/";

    // ── Professor creates a template ──────────────────────────
    public LabTemplate createTemplate(LabTemplate template) {
        String code = "LAB-" + UUID.randomUUID()
            .toString().substring(0, 6).toUpperCase();
        template.setShareCode(code);
        template.setCreatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    // ── List all templates ────────────────────────────────────
    public List<LabTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    // ── Get one template by ID ────────────────────────────────
    public Optional<LabTemplate> getTemplateById(Integer id) {
        return templateRepository.findById(id);
    }

    // ── Get template by share code ────────────────────────────
    public Optional<LabTemplate> getTemplateByCode(String code) {
        return templateRepository.findByShareCode(code);
    }

    // ── Spawn a lab instance ──────────────────────────────────
    public LabInstance spawnInstance(String shareCode, Integer userId) {
        LabTemplate template = templateRepository
            .findByShareCode(shareCode)
            .orElseThrow(() -> new RuntimeException(
                "Template not found: " + shareCode));

        List<Integer> usedPorts = instanceRepository.findAll()
            .stream()
            .map(LabInstance::getSshPort)
            .filter(p -> p != null)
            .collect(Collectors.toList());
        int sshPort = dockerService.findAvailablePort(usedPorts);

        LabInstance instance = new LabInstance();
        instance.setTemplateId(template.getId());
        instance.setUserId(userId);
        instance.setStatus("active");
        instance.setLabStatus("starting");
        instance.setNotes(template.getNotesScaffold());
        instance.setStartedAt(LocalDateTime.now());
        instance.setSshPort(sshPort);
        instance = instanceRepository.save(instance);

        String containerName = "lab-" + instance.getId();
        String volumePath = LAB_BASE_PATH + containerName;

        try {
            dockerService.startLabContainer(containerName, sshPort, volumePath);

            instance.setContainerId(containerName);
            instance.setLabStatus("running");
            instance = instanceRepository.save(instance);

            String tempPassword = "nexus-" + UUID.randomUUID()
                .toString().substring(0, 8);
            dockerService.addUserToContainer(containerName, "admin", tempPassword);

            LabMember adminMember = new LabMember();
            adminMember.setInstanceId(instance.getId());
            adminMember.setUserId(userId);
            adminMember.setLinuxUsername("admin");
            adminMember.setRole("admin");
            adminMember.setJoinedAt(LocalDateTime.now());
            memberRepository.save(adminMember);

            instance.setNotes(
                "## Lab Info\n" +
                "Container: " + containerName + "\n" +
                "SSH Port:  " + sshPort + "\n" +
                "Admin password: " + tempPassword + "\n\n" +
                "## Goal\n\n## What I built\n\n## What I learned"
            );
            instance = instanceRepository.save(instance);

        } catch (Exception e) {
            instance.setLabStatus("failed");
            instance.setNotes("Failed to start container: " + e.getMessage());
            instanceRepository.save(instance);
            throw new RuntimeException("Failed to start lab: " + e.getMessage());
        }

        return instance;
    }

    // ── Add a member to an existing lab ──────────────────────
    public LabMember addMemberToLab(Integer instanceId,
                                    Integer userId,
                                    String username,
                                    String password) {
        LabInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException(
                "Lab not found: " + instanceId));

        try {
            dockerService.addUserToContainer(
                instance.getContainerId(), username, password);

            LabMember member = new LabMember();
            member.setInstanceId(instanceId);
            member.setUserId(userId);
            member.setLinuxUsername(username);
            member.setRole("member");
            member.setJoinedAt(LocalDateTime.now());
            return memberRepository.save(member);

        } catch (Exception e) {
            throw new RuntimeException("Failed to add member: " + e.getMessage());
        }
    }

    // ── Remove a member from a lab ────────────────────────────
    public void removeMemberFromLab(Integer instanceId, String username) {
        LabInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException(
                "Lab not found: " + instanceId));

        try {
            dockerService.removeUserFromContainer(
                instance.getContainerId(), username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove member: " + e.getMessage());
        }
    }

    // ── Stop a lab ────────────────────────────────────────────
    public LabInstance stopLab(Integer instanceId) {
        LabInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException(
                "Lab not found: " + instanceId));

        try {
            dockerService.stopLabContainer(instance.getContainerId());
            instance.setLabStatus("stopped");
            instance.setStatus("completed");
            instance.setCompletedAt(LocalDateTime.now());
            return instanceRepository.save(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop lab: " + e.getMessage());
        }
    }

    // ── Get instances ─────────────────────────────────────────
    public List<LabInstance> getInstancesByUser(Integer userId) {
        return instanceRepository.findByUserId(userId);
    }

    public Optional<LabInstance> getInstanceById(Integer id) {
        return instanceRepository.findById(id);
    }

    // ── Get members of a lab ──────────────────────────────────
    public List<LabMember> getLabMembers(Integer instanceId) {
        return memberRepository.findByInstanceId(instanceId);
    }

    // ── Mark instance complete ────────────────────────────────
    public LabInstance completeInstance(Integer instanceId) {
        LabInstance instance = instanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException(
                "Instance not found: " + instanceId));
        instance.setStatus("completed");
        instance.setCompletedAt(LocalDateTime.now());
        return instanceRepository.save(instance);
    }
}