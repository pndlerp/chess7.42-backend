package com.bebrample.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClient pythonClient(){
        return RestClient.builder()
                .baseUrl("http://localhost:5050")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
