package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Hit;
import ru.practicum.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query(value = """
        SELECT new ru.practicum.StatsDto(app, uri, COUNT(DISTINCT ip) AS hits)
        FROM Hit
        WHERE timestamp BETWEEN :start AND :end
        AND (uri IN :uris OR :uris IS NULL)
        GROUP BY app, uri
        ORDER BY hits DESC
    """)
    List<StatsDto> findUniqueStatsByUrisAndTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );

    @Query(value = """
        SELECT new ru.practicum.StatsDto(app, uri, COUNT(ip) AS hits)
        FROM Hit
        WHERE timestamp BETWEEN :start AND :end
        AND (uri IN :uris OR :uris IS NULL)
        GROUP BY app, uri
        ORDER BY hits DESC
    """)
    List<StatsDto> findStatsByUrisAndTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris
    );
}
