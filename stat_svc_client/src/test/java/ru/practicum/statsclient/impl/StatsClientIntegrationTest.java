package ru.practicum.statsclient.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
        // Given
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // When
        statsClient.saveHit("ewm-main-service", "/events/1", "192.168.1.1");

        // Then
        verify(restTemplate, times(1)).postForEntity(
                eq("/hit"),
                any(),
                eq(Object.class)
        );
    }

    @Test
    void isAvailable_ServiceUp() {
        // Given
        when(restTemplate.getForEntity(eq("/health"), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // When
        boolean available = statsClient.isAvailable();

        // Then
        assertTrue(available);
    }

    @Test
    void isAvailable_ServiceDown() {
        // Given
        when(restTemplate.getForEntity(eq("/health"), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        boolean available = statsClient.isAvailable();

        // Then
        assertFalse(available);
    }

    @Test
    void saveHit_ExceptionHandling() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertDoesNotThrow(() ->
                statsClient.saveHit("ewm-main-service", "/events/1", "192.168.1.1")
        );
    }
}