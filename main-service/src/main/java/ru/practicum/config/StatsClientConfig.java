package ru.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.impl.StatsClientImpl;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClientImpl statsClient() {
        String statsServerUrl = System.getenv().getOrDefault("STATS_SERVER_URL", "http://stats-server:9090");
        return new StatsClientImpl(statsServerUrl);
    }
}