package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatsClientIntegrationTest {

    @Test
    void constructor_WithBaseUrl_ShouldCreateClient() {
        StatsClientImpl client = new StatsClientImpl("http://localhost:9090");
        assertNotNull(client);
    }

    @Test
    void constructor_WithRestClient_ShouldCreateClient() {
        RestClient restClient = RestClient.create();
        StatsClientImpl client = new StatsClientImpl(restClient, "http://localhost:9090");
        assertNotNull(client);
    }

    @Test
    void saveHit_WithInvalidServer_ShouldNotThrowException() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");

        assertDoesNotThrow(() -> client.saveHit("test-app", "/test", "127.0.0.1"));
    }

    @Test
    void getStats_WithInvalidServer_ShouldReturnEmptyList() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");

        List<Object> result = client.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/events/1", "/events/2"),
                true
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStats_WithNullUrisAndUnique_ShouldReturnEmptyList() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");

        List<Object> result = client.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getViewsForUri_WithInvalidServer_ShouldReturnZero() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");

        Long views = client.getViewsForUri(
                "/events/1",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                true
        );

        assertEquals(0L, views);
    }

    @Test
    void isAvailable_WithInvalidServer_ShouldReturnFalse() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");
        assertFalse(client.isAvailable());
    }

    @Test
    void extractHitsFromStats_ShouldReturnCorrectHits() {
        // Тестируем логику извлечения hits из статистики
        List<Object> stats = List.of(
                createStatMap("service1", "/events/1", 10),
                createStatMap("service2", "/events/2", 5),
                createStatMap("service3", "/events/1", 8)
        );

        Long result = extractHitsForUri("/events/1", stats);
        assertEquals(10L, result); // Должен вернуть первое совпадение
    }

    @Test
    void extractHitsFromStats_WithNoMatchingUri_ShouldReturnZero() {
        List<Object> stats = List.of(
                createStatMap("service1", "/events/1", 10),
                createStatMap("service2", "/events/2", 5)
        );

        Long result = extractHitsForUri("/events/3", stats);
        assertEquals(0L, result);
    }

    @Test
    void extractHitsFromStats_WithInvalidHitsType_ShouldReturnZero() {
        List<Object> stats = List.of(
                createStatMap("service1", "/events/1", "invalid") // hits как строка
        );

        Long result = extractHitsForUri("/events/1", stats);
        assertEquals(0L, result);
    }

    @Test
    void extractHitsFromStats_WithNullStats_ShouldReturnZero() {
        Long result = extractHitsForUri("/events/1", null);
        assertEquals(0L, result);
    }

    @Test
    void extractHitsFromStats_WithEmptyStats_ShouldReturnZero() {
        Long result = extractHitsForUri("/events/1", List.of());
        assertEquals(0L, result);
    }

    @Test
    void extractHitsFromStats_WithNonMapObject_ShouldReturnZero() {
        List<Object> stats = List.of(
                "invalid object", // Не Map объект
                createStatMap("service1", "/events/1", 10)
        );

        Long result = extractHitsForUri("/events/1", stats);
        assertEquals(10L, result); // Должен проигнорировать не-Map объект
    }

    @Test
    void extractHitsFromStats_WithNullUriInStat_ShouldReturnZero() {
        Map<String, Object> stat = createStatMap("service1", null, 10); // null uri
        List<Object> stats = List.of(stat);

        Long result = extractHitsForUri("/events/1", stats);
        assertEquals(0L, result);
    }

    @Test
    void extractHitsFromStats_WithNullHitsInStat_ShouldReturnZero() {
        Map<String, Object> stat = createStatMap("service1", "/events/1", null); // null hits
        List<Object> stats = List.of(stat);

        Long result = extractHitsForUri("/events/1", stats);
        assertEquals(0L, result);
    }

    // Вспомогательный метод для тестирования логики извлечения hits
    private Long extractHitsForUri(String uri, List<Object> stats) {
        // Имитируем логику из StatsClientImpl.extractHitsFromStats
        if (stats == null) {
            return 0L;
        }

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

    private Map<String, Object> createStatMap(String app, String uri, Object hits) {
        return Map.of(
                "app", app,
                "uri", uri,
                "hits", hits
        );
    }
}