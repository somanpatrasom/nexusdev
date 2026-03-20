package com.nexusdev.network;

import com.nexusdev.service.TransferService;
import java.io.*;
import java.net.*;

public class FileClient {

    private static final int PORT = 9090;
    private TransferService transferService = new TransferService();

    public void sendFile(String targetIP, String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("   File not found: " + filePath);
            return;
        }

        System.out.println("\n   Sending: " + file.getName());
        System.out.println("  To:        " + targetIP + ":" + PORT);
        System.out.println("  Size:      " + file.length() + " bytes");

        try (
            Socket socket = new Socket(targetIP, PORT);
            DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
            DataInputStream in =
                new DataInputStream(socket.getInputStream());
            FileInputStream fis = new FileInputStream(file)
        ) {
            // Send filename
            byte[] nameBytes = file.getName().getBytes();
            out.writeInt(nameBytes.length);
            out.write(nameBytes);

            // Send file size
            out.writeLong(file.length());

            // Send file bytes
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalSent = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
                int percent = (int)(totalSent * 100 / file.length());
                System.out.print("  Progress: " + percent + "%\r");
            }

            // Wait for confirmation
            String response = in.readUTF();
            if (response.equals("SUCCESS")) {
                System.out.println("\n   File sent successfully!");
                transferService.logTransfer(file.getName(),
                    file.length(), null, null, "DOWNLOAD", "COMPLETED");
            }

        } catch (IOException e) {
            System.out.println("\n   Send failed: " + e.getMessage());
        }
    }
}