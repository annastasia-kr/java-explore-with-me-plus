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
        statsClientHelper.recordEventView("app1", "/events/1", "192.168.1.1");

        verify(statsClient, times(1)).saveHit("app1", "/events/1", "192.168.1.1");
    }

    @Test
    void recordEventListViews_ShouldCallSaveHitWithEventsUri() {
        statsClientHelper.recordEventListViews("app1", "192.168.1.1");

        verify(statsClient, times(1)).saveHit("app1", "/events", "192.168.1.1");
    }

    @Test
    void getEventViews_ShouldCallGetViewsForUri() {
        when(statsClient.getViewsForUri(anyString(), any(), any(), eq(true))).thenReturn(10L);

        Long views = statsClientHelper.getEventViews(1L, LocalDateTime.now().minusDays(1));

        assertEquals(10L, views);
        verify(statsClient, times(1)).getViewsForUri(
                eq("/events/1"), any(LocalDateTime.class), any(LocalDateTime.class), eq(true));
    }

    @Test
    void getEventViews_WithNullEventDate_ShouldUseDefaultStartDate() {
        when(statsClient.getViewsForUri(anyString(), any(), any(), eq(true))).thenReturn(5L);

        Long views = statsClientHelper.getEventViews(1L, null);

        assertEquals(5L, views);
    }

    @Test
    void isStatsServiceAvailable_ShouldDelegateToClient() {
        when(statsClient.isAvailable()).thenReturn(true);

        assertTrue(statsClientHelper.isStatsServiceAvailable());
        verify(statsClient, times(1)).isAvailable();
    }
}