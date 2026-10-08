package com.ollacercana.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.ollacercana.persistence.repository.mongo.EventoReservaRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;

@TestConfiguration
@Profile("test-no-mongo")
public class MongoTestMockConfig {

    @Bean
    @Primary
    public NotificacionRepository notificacionRepository() {
        return Mockito.mock(NotificacionRepository.class);
    }

    @Bean
    @Primary
    public EventoReservaRepository eventoReservaRepository() {
        return Mockito.mock(EventoReservaRepository.class);
    }
}