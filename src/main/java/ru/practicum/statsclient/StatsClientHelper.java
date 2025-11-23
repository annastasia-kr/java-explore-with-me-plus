package ru.practicum.statsclient;

import ru.practicum.statsclient.config.StatsClientImpl;

import java.time.LocalDateTime;

public class StatsClientHelper {
    private final StatsClient statsClient;

    public StatsClientHelper() {
        this.statsClient = new StatsClientImpl();
    }

    public StatsClientHelper(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    public void recordEventView(String app, String uri, String ip) {
        statsClient.saveHit(app, uri, ip);
    }

    public void recordEventListViews(String app, String ip) {
        statsClient.saveHit(app, "/events", ip);
    }

    public Long getEventViews(Long eventId, LocalDateTime eventDate) {
        String eventUri = "/events/" + eventId;
        LocalDateTime start = (eventDate != null) ? eventDate : LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();
        return statsClient.getViewsForUri(eventUri, start, end, true);
    }

    public boolean isStatsServiceAvailable() {
        return statsClient.isAvailable();
    }
}