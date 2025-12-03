package ru.practicum.requests.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.requests.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;
@SuppressWarnings("unused")
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByEventId(Long eventId);
}