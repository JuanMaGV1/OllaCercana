package com.ollacercana.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.MongoRepository;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ollacercana.persistence.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = MongoRepository.class
        )
)
public class JpaConfig {
}