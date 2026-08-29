package com.example.api_docker.infra.kafka;

import com.example.api_docker.domain.shared.DomainEvent;
import com.example.api_docker.domain.user.event.UserCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Vamos usar essa classe para mapear os eventos. Isso evita que o domínio conheça o kafka
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicRegistry {

    private static final Map<Class<? extends DomainEvent>, String> TOPICS = Map.ofEntries(
            Map.entry(UserCreatedEvent.class,        "admin.created")
    );

    public String topicFor(DomainEvent event) {
        String topic = TOPICS.get(event.getClass());
        if (topic == null) {
            throw new IllegalArgumentException(
                    "Evento sem tópico mapeado: " + event.getClass().getSimpleName()
            );
        }
        return topic;
    }
}
