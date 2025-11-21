package ru.practicum.statsclient;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void saveHit(String app, String uri, String ip);
    List<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
    Long getViewsForUri(String uri, LocalDateTime start, LocalDateTime end, Boolean unique);
    boolean isAvailable();
}