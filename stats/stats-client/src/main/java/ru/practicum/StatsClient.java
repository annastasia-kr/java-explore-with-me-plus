package ru.practicum;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void saveHit(String app, String uri, String ip);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

}