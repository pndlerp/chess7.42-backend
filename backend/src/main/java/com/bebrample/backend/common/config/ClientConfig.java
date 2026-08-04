package com.bebrample.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {


    @Bean
    public RestClient pythonClient(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        return RestClient.builder()
                .baseUrl("http://localhost:8000")
                .requestFactory(requestFactory)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
