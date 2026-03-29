package com.nexusdev.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.nexusdev.websocket.TerminalWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TerminalWebSocketHandler terminalHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // ws://localhost:8080/terminal/3
        // the {instanceId} tells us which lab container to connect to
        registry.addHandler(terminalHandler, "/terminal/{instanceId}")
                .setAllowedOrigins("*");
    }
}