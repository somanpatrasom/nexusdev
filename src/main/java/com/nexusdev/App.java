package com.nexusdev;

import com.nexusdev.service.SessionService;
import com.nexusdev.service.DeviceService;
import com.nexusdev.service.StatsService;
import com.nexusdev.service.TransferService;
import com.nexusdev.network.FileServer;
import com.nexusdev.network.FileClient;
import com.nexusdev.db.DatabaseConnection;
import java.sql.*;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        SessionService sessionService = new SessionService();
        DeviceService deviceService = new DeviceService();
        StatsService statsService = new StatsService();

        FileServer fileServer = new FileServer();
        FileClient fileClient = new FileClient();

        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║          NexusDev  v0.1              ║");
            System.out.println("║       Your Personal Dev Lab          ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1 → Log a session                   ║");
            System.out.println("║  2 → View all sessions               ║");
            System.out.println("║  3 → Register a device               ║");
            System.out.println("║  4 → View all devices                ║");
            System.out.println("║  5 → Stats dashboard                 ║");
            System.out.println("║  6 → Start file server               ║");
            System.out.println("║  7 → Send a file                     ║");
            System.out.println("║  8 → View transfers                  ║");
            System.out.println("║  9 → List lab templates              ║");
            System.out.println("║  10 → Spawn lab from code            ║");
            System.out.println("║  0 → Exit                            ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("\n  Enter choice → ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> sessionService.logSession(scanner);
                case "2" -> sessionService.viewSessions();
                case "3" -> deviceService.registerDevice(scanner);
                case "4" -> deviceService.viewDevices();
                case "5" -> statsService.showStats();
                case "6" -> new Thread(() -> fileServer.start()).start();
                case "7" -> {
                    System.out.print("\n  Target IP: ");
                    String ip = scanner.nextLine().trim();
                    System.out.print("  File path: ");
                    String path = scanner.nextLine().trim();
                    fileClient.sendFile(ip, path);
                }
                case "8" -> new TransferService().viewTransfers();
                case "9" -> listTemplates();
                case "10" -> spawnLabFromCLI(scanner);
                case "0" -> {
                    System.out.println("\n  Goodbye. NexusDev shutting down.\n");
                    running = false;
                }
                default -> System.out.println("\n  Invalid choice. Enter 0-10.");
            }
        }

        scanner.close();
    }
    // List all lab templates
    static void listTemplates() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id, name, topic, duration_minutes, share_code, created_at " +
                "FROM lab_templates ORDER BY created_at DESC"
            );
            System.out.println("\n  Lab Templates");
            System.out.println("  ─────────────────────────────────────────");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("  #" + rs.getInt("id") + " — " + rs.getString("name"));
                System.out.println("  Topic:    " + rs.getString("topic"));
                System.out.println("  Duration: " + rs.getInt("duration_minutes") + " mins");
                System.out.println("  Code:     " + rs.getString("share_code"));
                System.out.println("  Created:  " + rs.getTimestamp("created_at"));
                System.out.println("  ─────────────────────────────────────────");
            }
            if (!found) System.out.println("  No templates yet.");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // Spawn a lab instance from the CLI
    static void spawnLabFromCLI(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.print("\n  Share Code (e.g. LAB-DB06C7): ");
            String code = scanner.nextLine().trim();

            System.out.print("  Your User ID: ");
            int userId = Integer.parseInt(scanner.nextLine().trim());

            // Look up template
            PreparedStatement find = conn.prepareStatement(
                "SELECT id, notes_scaffold FROM lab_templates WHERE share_code = ?"
            );
            find.setString(1, code);
            ResultSet rs = find.executeQuery();

            if (!rs.next()) {
                System.out.println("  ❌ Template not found: " + code);
                return;
            }

            int templateId = rs.getInt("id");
            String scaffold = rs.getString("notes_scaffold");

            // Insert instance
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO lab_instances (template_id, user_id, status, notes) " +
                "VALUES (?, ?, 'active', ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setInt(1, templateId);
            insert.setInt(2, userId);
            insert.setString(3, scaffold);
            insert.executeUpdate();

            ResultSet keys = insert.getGeneratedKeys();
            if (keys.next()) {
                System.out.println("\n  ✅ Lab spawned! Instance ID: " + keys.getInt(1));
                System.out.println("  View at: http://localhost:8080/dashboard/lab/" + keys.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
}
