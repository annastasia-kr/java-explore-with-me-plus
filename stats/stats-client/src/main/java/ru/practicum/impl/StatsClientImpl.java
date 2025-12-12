package ru.practicum.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsClient;
import ru.practicum.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final String baseUrl;
    private final String app;

    @Autowired
    public StatsClientImpl(@Value("${stats-server.url:http://localhost:9090}") String baseUrl,
                           @Value("${stats-server.app:main-service}") String app) {
        this.baseUrl = baseUrl;
        this.app = app;
        this.restClient = RestClient.create(baseUrl);
    }

    public StatsClientImpl(RestClient restClient, String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.app = "main-service";
    }

    public StatsClientImpl(RestClient restClient, String baseUrl, String app) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.app = app;
    }

    @Override
    public void saveHit(HttpServletRequest request) {
        try {
            Map<String, Object> hitData = new HashMap<>();
            hitData.put("app", app);
            hitData.put("uri", request.getRequestURI());
            hitData.put("ip", request.getRemoteAddr());
            hitData.put("timestamp", LocalDateTime.now().format(FORMATTER));

            ResponseEntity<Void> response = restClient.post()
                    .uri("/hit")
                    .body(hitData)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Hit saved: app={}, uri={}, ip={}", app, request.getRequestURI(),
                        request.getRemoteAddr());
            } else {
                log.error("Failed to save hit. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error saving hit: {}", e.getMessage());
        }
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                uriBuilder.queryParam("uris", String.join(",", uris));
            }

            if (unique != null) {
                uriBuilder.queryParam("unique", unique);
            }

            String url = uriBuilder.build().toUriString();

            List response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatsDto>>() {
                    });

            return response;
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}