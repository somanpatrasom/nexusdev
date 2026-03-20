package com.nexusdev.service;

import com.nexusdev.db.DatabaseConnection;
import java.sql.*;
import java.util.Scanner;

public class DeviceService {

    // Register a new device
    public void registerDevice(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            System.out.print("\n  Device name: ");
            String name = scanner.nextLine();

            System.out.print("  IP address: ");
            String ip = scanner.nextLine();

            System.out.print("  MAC address (or press Enter to skip): ");
            String mac = scanner.nextLine();
            if (mac.isEmpty()) mac = null;

            String sql = "INSERT INTO devices (name, ip_address, mac_address) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, ip);
            stmt.setString(3, mac);
            stmt.executeUpdate();

            System.out.println("\n  Device registered successfully!");

        } catch (SQLException e) {
            System.out.println("\n  Error: " + e.getMessage());
        }
    }

    // View all devices
    public void viewDevices() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM devices ORDER BY first_seen DESC"
            );

            System.out.println("\n  Registered Devices");
            System.out.println("  ─────────────────────────────────────────");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("  Device #" + rs.getInt("id"));
                System.out.println("  Name:       " + rs.getString("name"));
                System.out.println("  IP Address: " + rs.getString("ip_address"));
                System.out.println("  MAC:        " + rs.getString("mac_address"));
                System.out.println("  First Seen: " + rs.getTimestamp("first_seen"));
                System.out.println("  ─────────────────────────────────────────");
            }

            if (!found) System.out.println("  No devices registered yet.");

        } catch (SQLException e) {
            System.out.println("\n  Error: " + e.getMessage());
        }
    }
}
