package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private StatsClientImpl statsClient;

    @Test
    void saveHit_Success() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        statsClient.saveHit("test-app", "/test", "127.0.0.1");

        verify(restTemplate, times(1)).postForEntity(eq("/hit"), any(), eq(Object.class));
    }

    @Test
    void saveHit_WithDifferentApp() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        statsClient.saveHit("different-app", "/events/5", "10.0.0.1");

        verify(restTemplate, times(1)).postForEntity(eq("/hit"), any(), eq(Object.class));
    }

    @Test
    void isAvailable_ServiceUp() {
        when(restTemplate.getForEntity(eq("/health"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        assertTrue(statsClient.isAvailable());
    }

    @Test
    void isAvailable_ServiceDown() {
        when(restTemplate.getForEntity(eq("/health"), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertFalse(statsClient.isAvailable());
    }

    @Test
    void saveHit_ExceptionHandling() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertDoesNotThrow(() -> statsClient.saveHit("another-app", "/test", "192.168.1.1"));
    }

    @Test
    void getStats_Success() {
        Map<String, Object> stat1 = createStatMap("main-service", "/events/1", 10);
        Map<String, Object> stat2 = createStatMap("main-service", "/events/2", 5);
        Object[] statsArray = new Object[]{stat1, stat2};

        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenReturn(new ResponseEntity<>(statsArray, HttpStatus.OK));

        List<Object> result = statsClient.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/events/1", "/events/2"),
                true
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getStats_WithDifferentServices() {
        Map<String, Object> stat1 = createStatMap("service-a", "/events/1", 10);
        Map<String, Object> stat2 = createStatMap("service-b", "/events/2", 8);
        Object[] statsArray = new Object[]{stat1, stat2};

        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenReturn(new ResponseEntity<>(statsArray, HttpStatus.OK));

        List<Object> result = statsClient.getStats(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now(),
                List.of("/events/1", "/events/2"),
                false
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getViewsForUri_Success() {
        Map<String, Object> stat = createStatMap("events-service", "/events/1", 15);
        Object[] statsArray = new Object[]{stat};

        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenReturn(new ResponseEntity<>(statsArray, HttpStatus.OK));

        Long views = statsClient.getViewsForUri(
                "/events/1",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                true
        );

        assertEquals(15L, views);
    }

    @Test
    void getViewsForUri_NoStats() {
        Object[] statsArray = new Object[]{};

        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenReturn(new ResponseEntity<>(statsArray, HttpStatus.OK));

        Long views = statsClient.getViewsForUri(
                "/events/999",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                true
        );

        assertEquals(0L, views);
    }

    @Test
    void getViewsForUri_MultipleStats() {
        Map<String, Object> stat1 = createStatMap("service-1", "/events/1", 15);
        Map<String, Object> stat2 = createStatMap("service-2", "/events/2", 25);
        Object[] statsArray = new Object[]{stat1, stat2};

        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenReturn(new ResponseEntity<>(statsArray, HttpStatus.OK));

        Long views = statsClient.getViewsForUri(
                "/events/1",
                LocalDateTime.now().minusHours(12),
                LocalDateTime.now(),
                true
        );

        assertEquals(15L, views);
    }

    @Test
    void getStats_Exception() {
        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenThrow(new RuntimeException("Network error"));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<Object> result = statsClient.getStats(start, end, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getViewsForUri_Exception() {
        when(restTemplate.getForEntity(anyString(), eq(Object[].class), anyMap()))
                .thenThrow(new RuntimeException("Network error"));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        assertEquals(0L, views);
    }

    private Map<String, Object> createStatMap(String app, String uri, int hits) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("app", app);
        stat.put("uri", uri);
        stat.put("hits", hits);
        return stat;
    }
}