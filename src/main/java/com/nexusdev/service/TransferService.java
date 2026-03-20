package com.nexusdev.service;

import com.nexusdev.db.DatabaseConnection;
import java.sql.*;

public class TransferService {

    public void logTransfer(String filename, long sizeBytes,
                            Integer fromDeviceId, Integer toDeviceId,
                            String direction, String status) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql = "INSERT INTO file_transfers " +
                         "(filename, size_bytes, from_device_id, " +
                         "to_device_id, direction, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, filename);
            stmt.setLong(2, sizeBytes);
            
            if (fromDeviceId == null) stmt.setNull(3, java.sql.Types.INTEGER);
            else stmt.setInt(3, fromDeviceId);

            if (toDeviceId == null) stmt.setNull(4, java.sql.Types.INTEGER);
            else stmt.setInt(4, toDeviceId);
            stmt.setString(5, direction);
            stmt.setString(6, status);
            stmt.executeUpdate();

            System.out.println("   Transfer logged to database.");

        } catch (SQLException e) {
            System.out.println("   Failed to log: " + e.getMessage());
        }
    }

    public void viewTransfers() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT ft.*, " +
                "d1.name as from_name, d2.name as to_name " +
                "FROM file_transfers ft " +
                "LEFT JOIN devices d1 ON ft.from_device_id = d1.id " +
                "LEFT JOIN devices d2 ON ft.to_device_id = d2.id " +
                "ORDER BY ft.transferred_at DESC"
            );

            System.out.println("\n   File Transfers");
            System.out.println("  ─────────────────────────────────────────");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("  File:      " + rs.getString("filename"));
                System.out.println("  Size:      " + rs.getLong("size_bytes") + " bytes");
                System.out.println("  From:      " + rs.getString("from_name"));
                System.out.println("  To:        " + rs.getString("to_name"));
                System.out.println("  Direction: " + rs.getString("direction"));
                System.out.println("  Status:    " + rs.getString("status"));
                System.out.println("  Time:      " + rs.getTimestamp("transferred_at"));
                System.out.println("  ─────────────────────────────────────────");
            }

            if (!found) System.out.println("  No transfers yet.");

        } catch (SQLException e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }
}