package com.example.comm.service;

import com.example.comm.DTO.ChatMeta;
import com.example.comm.DTO.WebSocketWrap;
import com.example.comm.enums.AMQP;
import com.example.comm.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ChatService {

    @Autowired
    AMQPService amqpService;

    public Mono<Void> chatMsgHandle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        HttpHeaders headers = handshakeInfo.getHeaders();
        String query = handshakeInfo.getUri().getQuery();
        Map<String, String> queryMap = getQueryMap(query);
        String id = queryMap.get("device_id");
        if (id == null || id.isEmpty() || id.isBlank()) {
            session.close(new CloseStatus(1016, "unauthorized"));
            return Mono.error(new Exception("unauthorized message"));
        }
        final Queue queue = (amqpService.isQueueExists(id, AMQP.SUB) != null ? amqpService.isQueueExists(id, AMQP.SUB) : amqpService.createSimpleQueue(id, AMQP.SUB));
        final Exchange exchange = (amqpService.isExchangeExists(id, AMQP.SUB) != null ? amqpService.isExchangeExists(id, AMQP.SUB) : amqpService.createDirectExchange(id, AMQP.PUB));
        Mono<Void> hd = session.receive()
                .flatMap(message -> {
                    switch (message.getType()) {
                        case TEXT:
                            String payload = message.getPayloadAsText(StandardCharsets.UTF_8);
                            textMessage(payload);
                            break;
                        case BINARY:
                            break;
                        case PING:
                            break;
                        case PONG:
                            break;
                        default:
                    }
                    return null;
                }).then();
        Mono<Void> send = session.send(Flux.create(sink -> WebSocketWrap.SENDER.put(id, new WebSocketWrap(id, session, sink, id+AMQP.SUB.name(), queue, id+AMQP.PUB.name(), exchange))));
        return Mono.zip(hd, send).then().doOnNext(l -> log.info(String.format("User: [%s] connected...", id)));
    }

    private void textMessage(String payload) {
        ChatMeta meta = JsonUtils.toStruct(payload, ChatMeta.class);
        if (WebSocketWrap.SENDER.containsKey(meta.to)) {
            WebSocketWrap.SENDER.get(meta.to).sendText(payload);
        }
    }

    private Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (!query.isEmpty() || !query.isBlank()) {
            String[] params = query.split("&");
            Arrays.stream(params).forEach(s -> {
                String[] kv = s.split("=");
                map.put(kv[0], (kv.length > 1 ? kv[1] : ""));
            });
        }
        return map;
    }
}
