package ru.practicum.service;

import ru.practicum.HitDto;
import ru.practicum.StatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsService {

    HitDto create(HitDto hitDto);

    Collection<StatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
