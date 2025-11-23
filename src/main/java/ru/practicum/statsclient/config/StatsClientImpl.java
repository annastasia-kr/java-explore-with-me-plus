package ru.practicum.statsclient.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(StatsClientImpl.class);

    private final RestClient restClient;
    private final String baseUrl;

    public StatsClientImpl() {
        this("http://stats-server:9090");
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

    @Override
    public Long getViewsForUri(String uri, LocalDateTime start, LocalDateTime end, Boolean unique) {
        try {
            List<Object> stats = getStats(start, end, List.of(uri), unique);
            return extractHitsFromStats(uri, stats);
        } catch (Exception e) {
            log.error("Error getting views for uri {}: {}", uri, e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ResponseEntity<Void> response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Stats service unavailable: {}", e.getMessage());
            return false;
        }
    }

    private Long extractHitsFromStats(String uri, List<Object> stats) {
        for (Object stat : stats) {
            if (stat instanceof Map<?, ?> statMap) {
                Object statUri = statMap.get("uri");
                Object hits = statMap.get("hits");

                if (uri.equals(statUri) && hits instanceof Number) {
                    return ((Number) hits).longValue();
                }
            }
        }
        return 0L;
    }
}