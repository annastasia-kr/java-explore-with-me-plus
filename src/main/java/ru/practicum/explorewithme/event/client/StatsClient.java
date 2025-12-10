package ru.practicum.explorewithme.event.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String statsServiceUrl = "http://localhost:9090";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveHit(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String ip = request.getRemoteAddr();

            Map<String, Object> hit = new HashMap<>();
            hit.put("app", "ewm-main-service");
            hit.put("uri", uri);
            hit.put("ip", ip);
            hit.put("timestamp", LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(hit, headers);

            ResponseEntity<Object> response = restTemplate.postForEntity(
                    statsServiceUrl + "/hit",
                    entity,
                    Object.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Hit saved successfully for URI: {}", uri);
            } else {
                log.warn("Failed to save hit for URI: {}, status: {}", uri, response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error saving hit to stats service", e);
        }
    }

    public Object getStats(String start, String end, List<String> uris, Boolean unique) {
        try {
            String url = statsServiceUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}";

            Map<String, String> params = new HashMap<>();
            params.put("start", start);
            params.put("end", end);
            params.put("uris", String.join(",", uris));
            params.put("unique", String.valueOf(unique));

            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

        } catch (Exception e) {
            log.error("Error getting stats from stats service", e);
        }

        return List.of();
    }
}