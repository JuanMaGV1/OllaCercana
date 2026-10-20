package com.ollacercana.config;

import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test-no-mongo")
public class MongoTestMockConfig {

    @Bean
    @Primary
    public NotificacionRepository notificacionRepository() {
        return mock(NotificacionRepository.class);
    }

    @Bean
    @Primary
    public EventoReservaRepository eventoReservaRepository() {
        return mock(EventoReservaRepository.class);
    }

    @Bean
    @Primary
    public MensajeChatRepository mensajeChatRepository() {
        return mock(MensajeChatRepository.class);
    }
}