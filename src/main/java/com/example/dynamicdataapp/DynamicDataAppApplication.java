package com.example.dynamicdataapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class DynamicDataAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(DynamicDataAppApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DynamicDataAppApplication.class, args);
    }

    // Method to log the DB_URL (no @Bean annotation)
    private void printDbUrl(Environment env) {
        if (logger.isInfoEnabled()) {
            logger.info("DB_URL: {}", env.getProperty("DB_URL"));
        }
    }

    // Use CommandLineRunner to execute the printDbUrl method during startup
    @Bean
    public CommandLineRunner commandLineRunner(Environment env) {
        return args -> printDbUrl(env);
    }
}