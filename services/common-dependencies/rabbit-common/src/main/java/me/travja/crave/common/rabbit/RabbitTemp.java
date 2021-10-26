package me.travja.crave.common.rabbit;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@NoArgsConstructor
public class RabbitTemp extends RabbitTemplate {
    public RabbitTemp(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }
}
