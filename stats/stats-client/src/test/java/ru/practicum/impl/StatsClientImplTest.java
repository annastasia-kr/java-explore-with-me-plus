package ru.practicum.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;
import ru.practicum.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsClientImplTest {

    @Test
    void constructor_WithBaseUrl_ShouldCreateClient() {
        StatsClientImpl client = new StatsClientImpl(RestClient.create(), "http://localhost:9090", "main-service");
        assertNotNull(client);
    }

    @Test
    void saveHit_WithInvalidServer_ShouldNotThrow() {
        StatsClientImpl client = new StatsClientImpl(RestClient.create(), "http://invalid-server:9999", "main-service");
        // Создаем mock HttpServletRequest для теста
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/test");
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        assertDoesNotThrow(() -> client.saveHit(mockRequest));
    }

    @Test
    void getStats_WithInvalidServer_ShouldReturnEmptyList() {
        StatsClientImpl client = new StatsClientImpl(RestClient.create(), "http://invalid-server:9999", "main-service");

        List<StatsDto> result = client.getStats(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/test"),
                true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}