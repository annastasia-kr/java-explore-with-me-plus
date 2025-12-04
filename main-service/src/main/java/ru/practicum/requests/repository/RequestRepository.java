package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.enums.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("SELECT COUNT(r) FROM Request r " +
            "WHERE r.event.id = :eventId AND r.status = ru.practicum.requests.enums.RequestStatus.CONFIRMED")
    Long countConfirmedRequests(@Param("eventId") Long eventId);
}
