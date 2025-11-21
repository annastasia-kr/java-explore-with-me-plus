package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class StatsClientIntegrationTest {

    private StatsClientImpl statsClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate testRestTemplate = new RestTemplate();
        statsClient = new StatsClientImpl(testRestTemplate);
        mockServer = MockRestServiceServer.createServer(testRestTemplate);
    }

    @Test
    void saveHit_Integration_Success() {
        // Given
        mockServer.expect(requestTo("/hit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        // When
        statsClient.saveHit("ewm-main-service", "/events/1", "192.168.1.1");

        // Then
        mockServer.verify();
    }

    @Test
    void getStats_Integration_Success() {
        // Given
        String expectedResponse = "[" +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/1\", \"hits\": 10}," +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/2\", \"hits\": 5}" +
                "]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1,/events/2&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<String> uris = List.of("/events/1", "/events/2");

        // When
        List<Object> result = statsClient.getStats(start, end, uris, true);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        mockServer.verify();
    }

    @Test
    void getStats_WithPartialParameters() {
        // Given
        String expectedResponse = "[]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        List<Object> result = statsClient.getStats(start, end, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    void isAvailable_Integration_ServiceUp() {
        // Given
        mockServer.expect(requestTo("/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        // When
        boolean available = statsClient.isAvailable();

        // Then
        assertTrue(available);
        mockServer.verify();
    }

    @Test
    void isAvailable_Integration_ServiceDown() {
        // Given
        mockServer.expect(requestTo("/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        // When
        boolean available = statsClient.isAvailable();

        // Then
        assertFalse(available);
        mockServer.verify();
    }

    @Test
    void getViewsForUri_Integration_Success() {
        // Given
        String expectedResponse = "[" +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/1\", \"hits\": 15}" +
                "]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        // Then
        assertEquals(15L, views);
        mockServer.verify();
    }

    @Test
    void saveHit_ServerError_ShouldNotThrow() {
        // Given
        mockServer.expect(requestTo("/hit"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // When & Then
        assertDoesNotThrow(() ->
                statsClient.saveHit("ewm-main-service", "/events/1", "192.168.1.1")
        );
        mockServer.verify();
    }

    @Test
    void getViewsForUri_NoStats_ReturnsZero() {
        // Given
        String expectedResponse = "[]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/999&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        Long views = statsClient.getViewsForUri("/events/999", start, end, true);

        // Then
        assertEquals(0L, views);
        mockServer.verify();
    }

    @Test
    void getStats_Exception_ReturnsEmptyList() {
        // Given
        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        List<Object> result = statsClient.getStats(start, end, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    void getViewsForUri_Exception_ReturnsZero() {
        // Given
        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        // Then
        assertEquals(0L, views);
        mockServer.verify();
    }

    @Test
    void getStats_WithEmptyUris() {
        // Given
        String expectedResponse = "[" +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events\", \"hits\": 100}" +
                "]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&unique=false"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        List<Object> result = statsClient.getStats(start, end, List.of(), false);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        mockServer.verify();
    }

    @Test
    void getViewsForUri_WithMultipleStats() {
        // Given
        String expectedResponse = "[" +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/1\", \"hits\": 15}," +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/2\", \"hits\": 25}" +
                "]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        // Then
        assertEquals(15L, views);
        mockServer.verify();
    }

    @Test
    void getViewsForUri_NonMatchingUri_ReturnsZero() {
        // Given
        String expectedResponse = "[" +
                "{\"app\": \"ewm-main-service\", \"uri\": \"/events/2\", \"hits\": 25}" +
                "]";

        mockServer.expect(requestTo("/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        // When
        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        // Then
        assertEquals(0L, views);
        mockServer.verify();
    }
}