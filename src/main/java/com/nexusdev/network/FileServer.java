package com.nexusdev.network;

import com.nexusdev.service.TransferService;
import java.io.*;
import java.net.*;

public class FileServer {

    private static final int PORT = 9090;
    private TransferService transferService = new TransferService();

    public void start() {
        System.out.println("\n   NexusDev File Server starting...");
        System.out.println("  Listening on port " + PORT);
        System.out.println("  Press Ctrl+C to stop\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                String senderIP = socket.getInetAddress().getHostAddress();
                System.out.println("   Connection from: " + senderIP);
                new Thread(() -> handleTransfer(socket, senderIP)).start();
            }

        } catch (IOException e) {
            System.out.println("   Server error: " + e.getMessage());
        }
    }

    private void handleTransfer(Socket socket, String senderIP) {
        try (
            DataInputStream in =
                new DataInputStream(socket.getInputStream());
            DataOutputStream out =
                new DataOutputStream(socket.getOutputStream())
        ) {
            // Read filename
            int nameLength = in.readInt();
            byte[] nameBytes = new byte[nameLength];
            in.readFully(nameBytes);
            String filename = new String(nameBytes);

            // Read file size
            long fileSize = in.readLong();
            System.out.println("   Receiving: " + filename +
                               " (" + fileSize + " bytes)");

            // Save file to ~/nexusdev-files/
            String saveDir = System.getProperty("user.home") +
                             "/nexusdev-files/";
            new File(saveDir).mkdirs();

            try (FileOutputStream fos =
                     new FileOutputStream(saveDir + filename)) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;
                int bytesRead;

                while (remaining > 0) {
                    bytesRead = in.read(buffer, 0,
                        (int) Math.min(buffer.length, remaining));
                    fos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }

            // Confirm receipt
            out.writeUTF("SUCCESS");
            System.out.println("   Saved to: " + saveDir + filename);

            // Log to database
            transferService.logTransfer(filename, fileSize,
                                        null, null, "UPLOAD", "COMPLETED");

        } catch (IOException e) {
            System.out.println("   Transfer error: " + e.getMessage());
        }
    }
}