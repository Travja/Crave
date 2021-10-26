package me.travja.crave.common.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RabbitMqService {
    private final RabbitTemp rabbitTemplate;

    @Value("${RABBITMQ_EXCHANGE}")
    private String exchange;

    @Value("${RABBITMQ_ROUTING_KEY}")
    private String routingKey;

    public void send(Map<String, String> payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        System.out.println("Sent payload to RabbitMQ");
    }
}
