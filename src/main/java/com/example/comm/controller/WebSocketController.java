package com.example.comm.controller;

import com.example.comm.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class WebSocketController implements WebSocketHandler {

    @Autowired
    ChatService chatService;
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return chatService.chatMsgHandle(session);
    }

    @Override
    public List<String> getSubProtocols() {
        return WebSocketHandler.super.getSubProtocols();
    }
}
