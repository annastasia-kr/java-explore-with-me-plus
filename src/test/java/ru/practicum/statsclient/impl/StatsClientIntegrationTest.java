package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class StatsClientIntegrationTest {

    private StatsClientImpl statsClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://test-server:9090";

        RestTemplate restTemplate = new RestTemplate();
        RestClient restClient = RestClient.builder(restTemplate).build();

        statsClient = new StatsClientImpl(restClient, baseUrl);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void saveHit_Integration_Success() {
        mockServer.expect(requestTo("http://test-server:9090/hit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        statsClient.saveHit("ewm-main-service", "/events/1", "192.168.1.1");

        mockServer.verify();
    }

    @Test
    void getStats_Integration_Success() {
        String expectedResponse = "[{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"hits\":10}]";

        mockServer.expect(requestTo("http://test-server:9090/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        List<Object> result = statsClient.getStats(start, end, List.of("/events/1"), true);

        assertNotNull(result);
        assertEquals(1, result.size());
        mockServer.verify();
    }

    @Test
    void isAvailable_Integration_ServiceUp() {
        mockServer.expect(requestTo("http://test-server:9090/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        assertTrue(statsClient.isAvailable());
        mockServer.verify();
    }

    @Test
    void isAvailable_Integration_ServiceDown() {
        mockServer.expect(requestTo("http://test-server:9090/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertFalse(statsClient.isAvailable());
        mockServer.verify();
    }

    @Test
    void getViewsForUri_Integration_Success() {
        String expectedResponse = "[{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"hits\":15}]";

        mockServer.expect(requestTo("http://test-server:9090/stats?start=2024-01-01%2000:00:00&end=2024-01-02%2000:00:00&uris=/events/1&unique=true"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        Long views = statsClient.getViewsForUri("/events/1", start, end, true);

        assertEquals(15L, views);
        mockServer.verify();
    }
}