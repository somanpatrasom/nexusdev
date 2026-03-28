package com.nexusdev.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_members")
public class LabMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "instance_id")
    private Integer instanceId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "linux_username")
    private String linuxUsername;

    private String role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getInstanceId() { return instanceId; }
    public void setInstanceId(Integer instanceId) { this.instanceId = instanceId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getLinuxUsername() { return linuxUsername; }
    public void setLinuxUsername(String linuxUsername) { this.linuxUsername = linuxUsername; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
