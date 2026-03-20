package com.nexusdev.service;

import com.nexusdev.db.DatabaseConnection;
import java.sql.*;
import java.util.Scanner;

public class SessionService {

    // Log a new session
    public void logSession(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            System.out.print("\n  Topic: ");
            String topic = scanner.nextLine();

            System.out.print("  Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine());

            System.out.print("  Notes: ");
            String notes = scanner.nextLine();

            String sql = "INSERT INTO sessions (topic, duration_minutes, notes) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, topic);
            stmt.setInt(2, duration);
            stmt.setString(3, notes);
            stmt.executeUpdate();

            System.out.println("\n  Session logged successfully!");

        } catch (SQLException e) {
            System.out.println("\n  Error: " + e.getMessage());
        }
    }

    // View all sessions
    public void viewSessions() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM sessions ORDER BY created_at DESC"
            );

            System.out.println("\n  Learning Sessions");
            System.out.println("  ─────────────────────────────────────────");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("  Session #" + rs.getInt("id"));
                System.out.println("  Topic:    " + rs.getString("topic"));
                System.out.println("  Duration: " + rs.getInt("duration_minutes") + " mins");
                System.out.println("  Notes:    " + rs.getString("notes"));
                System.out.println("  Date:     " + rs.getTimestamp("created_at"));
                System.out.println("  ─────────────────────────────────────────");
            }

            if (!found) System.out.println("  No sessions logged yet.");

        } catch (SQLException e) {
            System.out.println("\n  Error: " + e.getMessage());
        }
    }
}
