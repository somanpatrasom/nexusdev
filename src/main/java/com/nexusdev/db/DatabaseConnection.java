package com.nexusdev.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL  = "jdbc:postgresql://localhost:5432/nexusdev";
    private static final String USER = "nexus_admin";
    private static final String PASS = "soman503";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
