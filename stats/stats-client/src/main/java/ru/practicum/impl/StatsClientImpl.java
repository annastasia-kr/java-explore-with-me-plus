package ru.practicum.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestClient restClient;
    private final String baseUrl;

    @Autowired
    public StatsClientImpl() {
        this("${stats-server.url}");
    }

    public StatsClientImpl(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create(baseUrl);
    }

    public StatsClientImpl(RestClient restClient, String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public void saveHit(String app, String uri, String ip) {
        try {
            Map<String, Object> hitData = new HashMap<>();
            hitData.put("app", app);
            hitData.put("uri", uri);
            hitData.put("ip", ip);
            hitData.put("timestamp", LocalDateTime.now().format(FORMATTER));

            ResponseEntity<Void> response = restClient.post()
                    .uri("/hit")
                    .body(hitData)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Hit saved: app={}, uri={}, ip={}", app, uri, ip);
            } else {
                log.error("Failed to save hit. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error saving hit: {}", e.getMessage());
        }
    }

    @Override
    public List<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
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

            Object[] response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Object[].class);

            return response != null ? Arrays.asList(response) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}