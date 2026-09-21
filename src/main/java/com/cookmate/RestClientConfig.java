package com.cookmate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// Spring Boot - Fast API 호출을 위한 Rest API 통신 WebClient는 비동기 처리 가능
@Configuration
public class RestClientConfig {
    @Bean
    public RestClient fastApiRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }
}
