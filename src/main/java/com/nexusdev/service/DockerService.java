package com.nexusdev.service;

import com.nexusdev.controller.DashboardController;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DockerService {
    
    private final DashboardController dashboardController;

    DockerService(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    // ── Run a shell command and return output ─────────────────
    // This is how Java runs Docker commands —
    // it spawns a subprocess just like typing in the terminal
    private String runCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                "Command failed (exit " + exitCode + "): " + output
            );
        }

        return output.toString().trim();
    }

    // ── Start a new lab container ─────────────────────────────
    public String startLabContainer(String containerName, 
                                    int sshPort,
                                    String volumePath) throws Exception {
        // Create the volume directory on the host first
        new java.io.File(volumePath).mkdirs();

        // docker run -d
        //   --name lab-1
        //   -p 2201:22
        //   -v /opt/nexusdev/lab/members/lab-1:/home
        //   --restart unless-stopped
        //   nexusdev-lab
        String containerId = runCommand(
            "docker", "run", "-d",
            "--name", containerName,
            "-p", sshPort + ":22",
            "-v", volumePath + ":/home",
            "--restart", "unless-stopped",
            "nexusdev-lab"
        );
        return containerId;
    }

    // ── Stop and remove a container ───────────────────────────
    public void stopLabContainer(String containerName) throws Exception {
        runCommand("docker", "stop", containerName);
        runCommand("docker", "rm", "containerName");
    }

    // ── Add a user to a running container ─────────────────────
    public void addUserToContainer(String containerName,
                                    String username,
                                    String password) throws Exception {
        // Create the Linux user with home directory
        runCommand("docker", "exec", containerName,
            "useradd", "-m", "-s", "/bin/bash", username);

        // Set their password
        runCommand("docker", "exec", containerName,
            "bash", "-c",
            "echo '" + username + ":" + password + "' | chpasswd");

        // Add to labmembers group (shared folder access)
        runCommand("docker", "exec", containerName,
            "usermod", "-aG", "labmembers", username);
    }

    // ── Remove a user from a container ───────────────────────
    public void removeUserFromContainer(String containerName,
                                        String username) throws Exception {
        // -r removes home directory too
        runCommand("docker", "exec", containerName,
            "userdel", "-r", username);
    }

    // ── Check if container is running ─────────────────────────
    public boolean isContainerRunning(String containerName) {
        try {
            String output = runCommand(
                "docker", "inspect",
                "--format={{.State.Running}}",
                containerName
            );
            return "true".equals(output.trim());
        } catch (Exception e) {
            return false;
        }
    }

    // ── Find an available SSH port ────────────────────────────
    // Starts at 2201, increments until a free port is found
    public int findAvailablePort(List<Integer> usedPorts) {
        int port = 2201;
        while (usedPorts.contains(port)) {
            port++;
        }
        return port;
    }
}
