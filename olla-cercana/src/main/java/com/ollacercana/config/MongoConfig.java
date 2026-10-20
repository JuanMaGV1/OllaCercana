package com.ollacercana.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.ollacercana.persistence.repository.mongo")
public class MongoConfig {
}