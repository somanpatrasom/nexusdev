package com.nexusdev.websocket;

import com.nexusdev.model.LabInstance;
import com.nexusdev.repository.LabInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private LabInstanceRepository instanceRepository;

    // Stores active terminal sessions
    // key = WebSocket session ID
    // value = the running docker exec process
    private final Map<String, Process> terminals = new ConcurrentHashMap<>();
    private final Map<String, Thread> outputThreads = new ConcurrentHashMap<>();

    // ── Called when browser connects ─────────────────────────
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();
        String instanceId = path.substring(path.lastIndexOf('/') + 1);
        System.out.println("=== TERMINAL DEBUG: path=" + path + " instanceId=" + instanceId);

        LabInstance instance = instanceRepository
            .findById(Integer.parseInt(instanceId))
            .orElse(null);

        if (instance == null || instance.getContainerId() == null) {
            session.sendMessage(new TextMessage(
                "\r\n\033[31mLab not found or not running.\033[0m\r\n"));
            session.close();
            return;
        }

        String containerName = instance.getContainerId();

        // Use 'script' to fake a TTY — this makes bash behave interactively
        // script -q /dev/null runs a shell session with a pseudo-terminal
        // without needing docker exec -t
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", "-i", containerName,
            "script", "-q", "-c", "bash", "/dev/null"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        terminals.put(session.getId(), process);

        // Send initial newline to trigger the prompt
        process.getOutputStream().write("\n".getBytes());
        process.getOutputStream().flush();

        Thread outputThread = new Thread(() -> {
            try {
                InputStream input = process.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    if (session.isOpen()) {
                        String output = new String(buffer, 0, bytesRead);
                        session.sendMessage(new TextMessage(output));
                    }
                }
            } catch (Exception e) {
                // Connection closed
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
        outputThreads.put(session.getId(), outputThread);
    }

    // ── Called when browser sends a keystroke ─────────────────
    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) throws Exception {
        Process process = terminals.get(session.getId());
        if (process != null && process.isAlive()) {
            // Write the keystroke into docker exec stdin
            OutputStream stdin = process.getOutputStream();
            stdin.write(message.getPayload().getBytes());
            stdin.flush();
        }
    }

    // ── Called when browser disconnects ──────────────────────
    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) throws Exception {
        // Kill the docker exec process
        Process process = terminals.remove(session.getId());
        if (process != null) {
            process.destroy();
        }
        Thread thread = outputThreads.remove(session.getId());
        if (thread != null) {
            thread.interrupt();
        }
    }
}
