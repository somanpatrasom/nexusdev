package com.nexusdev;

import com.nexusdev.service.SessionService;
import com.nexusdev.service.DeviceService;
import com.nexusdev.service.StatsService;
import com.nexusdev.service.TransferService;
import com.nexusdev.network.FileServer;
import com.nexusdev.network.FileClient;
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
            System.out.println("║  9 → Exit                            ║");
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
                    System.out.println("\n  Target IP:  ");
                    String ip = scanner.nextLine().trim();
                    System.out.println("    File path:  ");
                    String path = scanner.nextLine().trim();
                    fileClient.sendFile(ip, path);
                }
                case "8" -> new TransferService().viewTransfers();
                case "9" -> {
                    System.out.println("\n  Goodbye. NexusDev shutting down.\n");
                    running = false;
                }
                default -> System.out.println("\n  Invalid choice. Enter 1-6.");
            }
        }

        scanner.close();
    }
}
