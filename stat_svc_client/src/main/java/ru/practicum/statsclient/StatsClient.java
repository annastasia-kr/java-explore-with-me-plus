package ru.practicum.statsclient;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    /**
     * Сохранить информацию о запросе
     */
    void saveHit(String app, String uri, String ip);

    /**
     * Получить статистику по посещениям
     */
    List<Object> getStats(LocalDateTime start, LocalDateTime end,
                          List<String> uris, Boolean unique);

    /**
     * Получить количество просмотров для конкретного URI
     */
    Long getViewsForUri(String uri, LocalDateTime start, LocalDateTime end, Boolean unique);

    /**
     * Проверить доступность сервиса статистики
     */
    boolean isAvailable();
}