package ru.practicum.statsclient.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(StatsClientImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StatsClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
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
                log.debug("Hit successfully saved: app={}, uri={}, ip={}", app, uri, ip);
            } else {
                log.error("Failed to save hit. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error while saving hit: {}", e.getMessage());
        }
    }

    @Override
    public List<Object> getStats(LocalDateTime start, LocalDateTime end,
                                 List<String> uris, Boolean unique) {
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

            ResponseEntity<String> response = restTemplate.getForEntity(
                    urlBuilder.toString(), String.class, parameters);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<List<Object>>() {});
            }
        } catch (Exception e) {
            log.error("Error while getting stats: {}", e.getMessage());
        }

        return List.of();
    }

    @Override
    public Long getViewsForUri(String uri, LocalDateTime start, LocalDateTime end, Boolean unique) {
        try {
            List<Object> stats = getStats(start, end, List.of(uri), unique);

            for (Object stat : stats) {
                if (stat instanceof Map) {
                    Map<?, ?> statMap = (Map<?, ?>) stat;
                    if (uri.equals(statMap.get("uri")) && statMap.get("hits") instanceof Number) {
                        return ((Number) statMap.get("hits")).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while getting views for uri {}: {}", uri, e.getMessage());
        }

        return 0L;
    }

    @Override
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Stats service is not available: {}", e.getMessage());
            return false;
        }
    }
}