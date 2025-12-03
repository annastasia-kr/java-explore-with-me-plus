package ru.practicum.events.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.events.model.Event;
import ru.practicum.events.enums.StateEvent;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;


public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    Optional<Event> findByIdAndState(Long id, StateEvent state);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
