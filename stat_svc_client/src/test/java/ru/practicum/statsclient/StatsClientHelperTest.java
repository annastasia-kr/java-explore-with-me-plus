package ru.practicum.statsclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientHelperTest {

    @Mock
    private StatsClient statsClient;

    private StatsClientHelper statsClientHelper;

    @BeforeEach
    void setUp() {
        statsClientHelper = new StatsClientHelper(statsClient);
    }

    @Test
    void recordEventView_ShouldCallSaveHit() {
        // Given
        String app = "ewm-main-service";
        String uri = "/events/1";
        String ip = "192.168.1.1";

        // When
        statsClientHelper.recordEventView(app, uri, ip);

        // Then
        verify(statsClient, times(1)).saveHit(app, uri, ip);
    }

    @Test
    void recordEventListViews_ShouldCallSaveHitWithEventsUri() {
        // Given
        String app = "ewm-main-service";
        String ip = "192.168.1.1";

        // When
        statsClientHelper.recordEventListViews(app, ip);

        // Then
        verify(statsClient, times(1)).saveHit(app, "/events", ip);
    }

    @Test
    void getEventViews_ShouldCallGetViewsForUri() {
        // Given
        Long eventId = 1L;
        LocalDateTime eventDate = LocalDateTime.now().minusDays(1);

        when(statsClient.getViewsForUri(anyString(), any(), any(), eq(true)))
                .thenReturn(10L);

        // When
        Long views = statsClientHelper.getEventViews(eventId, eventDate);

        // Then
        assertEquals(10L, views);
        verify(statsClient, times(1)).getViewsForUri(
                eq("/events/1"), any(LocalDateTime.class), any(LocalDateTime.class), eq(true));
    }

    @Test
    void getEventViews_WithNullEventDate_ShouldUseDefaultStartDate() {
        // Given
        Long eventId = 1L;

        when(statsClient.getViewsForUri(anyString(), any(), any(), eq(true)))
                .thenReturn(5L);

        // When
        Long views = statsClientHelper.getEventViews(eventId, null);

        // Then
        assertEquals(5L, views);
        verify(statsClient, times(1)).getViewsForUri(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class), eq(true));
    }

    @Test
    void isStatsServiceAvailable_ShouldDelegateToClient() {
        // Given
        when(statsClient.isAvailable()).thenReturn(true);

        // When
        boolean available = statsClientHelper.isStatsServiceAvailable();

        // Then
        assertTrue(available);
        verify(statsClient, times(1)).isAvailable();
    }
}