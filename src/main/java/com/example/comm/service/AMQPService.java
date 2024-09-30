package com.example.comm.service;

import com.example.comm.enums.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AMQPService {
    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Queue isQueueExists(String queueName, AMQP mode) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive(queueName);
                return null;
            });
            return new Queue(queueName + mode.name(), true);
        } catch (Exception e) {
            return null;
        }
    }

    public Exchange isExchangeExists(String exchangeName, AMQP mode) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(exchangeName);
                return null;
            });
            return new DirectExchange(exchangeName + mode.name(), true, false);
        } catch (Exception e) {
            return null;
        }
    }

    public Queue createSimpleQueue(String name, AMQP mode) {
        Queue queue = new Queue(name + mode.name(), true);
        rabbitAdmin.declareQueue(queue);
        return queue;
    }

    public void deleteSimpleQueue(String name, AMQP mode) {
        rabbitAdmin.deleteQueue(name + mode.name());
    }

    public Exchange createDirectExchange(String id, AMQP mode) {
        DirectExchange directExchange = new DirectExchange(id + mode.name(), true, false);
        rabbitAdmin.declareExchange(directExchange);
        return directExchange;
    }

    public Binding bindingQToExchange(Queue infoQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(infoQueue).to(directExchange).withQueueName();
    }

    public Mono<Void> publishMessage(String name, String exchange, String msg) {
        return Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(exchange + AMQP.PUB.name(), name + AMQP.SUB.name(), msg));
    }
}
