# NexusDev

A self-hosted backend platform built from scratch on Linux (WSL Ubuntu).

## What It Does
- Personal learning journal — tracks every study session
- Remote dev lab — accessible from any device via SSH + Tailscale
- LAN file sharing — transfer files between devices (coming soon)
- REST API — web interface accessible from any browser (coming soon)

## Stack
- Java 21
- PostgreSQL 16
- Maven 3.8
- SSH + Tailscale

## Project Structure
```
src/main/java/com/nexusdev/
├── App.java                  # Entry point, CLI menu
├── db/
│   └── DatabaseConnection.java
└── service/
    ├── SessionService.java
    ├── DeviceService.java
    └── StatsService.java
```

## Sessions Completed
1. PostgreSQL schema design
2. Java + JDBC connection
3. Interactive CLI
4. SSH + Tailscale remote access
5. SSH key authentication
6. Stats dashboard + login tracking

## Built by
Soman — learning backend development from scratch
