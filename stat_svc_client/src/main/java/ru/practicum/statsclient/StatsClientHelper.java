package ru.practicum.statsclient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StatsClientHelper {
    private final StatsClient statsClient;

    public void recordEventView(String app, String uri, String ip) {
        statsClient.saveHit(app, uri, ip);
    }

    public void recordEventListViews(String app, String ip) {
        statsClient.saveHit(app, "/events", ip);
    }

    public Long getEventViews(Long eventId, LocalDateTime eventDate) {
        String eventUri = "/events/" + eventId;
        LocalDateTime start = eventDate != null ? eventDate : LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        return statsClient.getViewsForUri(eventUri, start, end, true);
    }

    public boolean isStatsServiceAvailable() {
        return statsClient.isAvailable();
    }
}