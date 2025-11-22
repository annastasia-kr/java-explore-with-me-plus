package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatsClientImplTest {

    @Test
    void constructor_ShouldCreateClient() {
        StatsClientImpl client = new StatsClientImpl();
        assertNotNull(client);
    }

    @Test
    void constructor_WithBaseUrl_ShouldCreateClient() {
        StatsClientImpl client = new StatsClientImpl("http://localhost:9090");
        assertNotNull(client);
    }

    @Test
    void saveHit_WithInvalidServer_ShouldNotThrow() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");
        assertDoesNotThrow(() -> client.saveHit("test-app", "/test", "127.0.0.1"));
    }

    @Test
    void getStats_WithInvalidServer_ShouldReturnEmptyList() {
        StatsClientImpl client = new StatsClientImpl("http://invalid-server:9999");

        List<Object> result = client.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/test"),
                true
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
}