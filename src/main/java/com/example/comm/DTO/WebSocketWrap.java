package com.example.comm.DTO;

import com.example.comm.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@Slf4j
public class WebSocketWrap {
    public static ConnectionFactory connectionFactory;

    public static final Map<String, WebSocketWrap> SENDER = new ConcurrentHashMap<>();
    private String id;
    private WebSocketSession session;
    private FluxSink<WebSocketMessage> sink;
    private final String qName;
    private final Queue q;
    private final String eName;
    private final Exchange exchange;

    static {
        purge();
    }
    @PostConstruct
    public void init() {
        startListening();
    }

    public static void setConnectionFactory(ConnectionFactory c) {
        connectionFactory = c;
    }

    public static void broadcastText(Objects obj) {
        SENDER.values().forEach(webSocketWrap -> webSocketWrap.sendText(obj));
    }

    public void sendText(Object obj) {
        sink.next(session.textMessage(JsonUtils.toJson(obj)));
    }

    public void handleMessage(String message) {
        log.info("Received message from queue [{}]: {}", qName, message);
        sendText(message); // 将消息发送给 WebSocket 客户端
    }

    private static void purge() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            new ArrayList<>(SENDER.values())
            SENDER.values().forEach(webSocketWrap -> {
                if (!webSocketWrap.session.isOpen()) {
                    log.warn(String.format("UserID: [%s] session: [%s] closed", webSocketWrap.id, webSocketWrap.session.getId()));
                    SENDER.remove(webSocketWrap.id);
                    webSocketWrap.session.close();
                }
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    public void startListening() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(qName);
        container.setMessageListener(new MessageListenerAdapter(this, "handleMessage"));
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

    }
}
