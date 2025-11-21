package ru.practicum.statsclient.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Creating RestTemplate bean with base URL: http://stats-server:9090");
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory("http://stats-server:9090"))
                .build();
    }
}