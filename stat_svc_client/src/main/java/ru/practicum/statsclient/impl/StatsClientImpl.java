package ru.practicum.statsclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(StatsClientImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StatsClientImpl() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://stats-server:9090"));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public StatsClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public StatsClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveHit(String app, String uri, String ip) {
        try {
            Map<String, Object> hitData = new HashMap<>();
            hitData.put("app", app);
            hitData.put("uri", uri);
            hitData.put("ip", ip);
            hitData.put("timestamp", LocalDateTime.now().format(FORMATTER));

            ResponseEntity<Object> response = restTemplate.postForEntity("/hit", hitData, Object.class);

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
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("start", start.format(FORMATTER));
            parameters.put("end", end.format(FORMATTER));

            StringBuilder urlBuilder = new StringBuilder("/stats?start={start}&end={end}");

            if (uris != null && !uris.isEmpty()) {
                parameters.put("uris", String.join(",", uris));
                urlBuilder.append("&uris={uris}");
            }

            if (unique != null) {
                parameters.put("unique", unique);
                urlBuilder.append("&unique={unique}");
            }

            ResponseEntity<Object[]> response = restTemplate.getForEntity(
                    urlBuilder.toString(), Object[].class, parameters);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
        }
        return Collections.emptyList();
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

    private Long extractHitsFromStats(String uri, List<Object> stats) {
        for (Object stat : stats) {
            // Используем pattern matching вместо явного cast
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

    @Override
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Stats service unavailable: {}", e.getMessage());
            return false;
        }
    }
}