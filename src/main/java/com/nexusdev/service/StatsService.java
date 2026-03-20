package com.nexusdev.service;

import com.nexusdev.db.DatabaseConnection;
import java.sql.*;

public class StatsService {

    public void showStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║          NexusDev Stats             ║");
            System.out.println("╠══════════════════════════════════════╣");

            // Total sessions
            ResultSet rs1 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) AS total FROM sessions"
            );
            rs1.next();
            System.out.println("║  Total sessions   → " + padRight(rs1.getInt("total") + "", 17) + "║");

            // Total study time
            ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT SUM(duration_minutes) AS total FROM sessions"
            );
            rs2.next();
            int totalMins = rs2.getInt("total");
            String timeStr = (totalMins / 60) + "h " + (totalMins % 60) + "mins";
            System.out.println("║  Total study time → " + padRight(timeStr, 17) + "║");

            // Total devices
            ResultSet rs3 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) AS total FROM devices"
            );
            rs3.next();
            System.out.println("║  Total devices    → " + padRight(rs3.getInt("total") + "", 17) + "║");

            // Most studied topic
            ResultSet rs4 = conn.createStatement().executeQuery(
                "SELECT topic, COUNT(*) AS cnt FROM sessions " +
                "GROUP BY topic ORDER BY cnt DESC LIMIT 1"
            );
            String topic = rs4.next() ? rs4.getString("topic") : "none";
            if (topic.length() > 17) topic = topic.substring(0, 14) + "...";
            System.out.println("║  Top topic        → " + padRight(topic, 17) + "║");

            // Last session date
            ResultSet rs5 = conn.createStatement().executeQuery(
                "SELECT MAX(created_at) AS last FROM sessions"
            );
            rs5.next();
            String last = rs5.getTimestamp("last").toString().substring(0, 16);
            System.out.println("║  Last session     → " + padRight(last, 17) + "║");

            System.out.println("╚══════════════════════════════════════╝");

        } catch (SQLException e) {
            System.out.println("\n  ❌ Error: " + e.getMessage());
        }
    }

    // Helper: pad a string to a fixed width
    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}