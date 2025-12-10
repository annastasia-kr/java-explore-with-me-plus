package ru.practicum.explorewithme.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.moderation.model.Event;
import ru.practicum.explorewithme.moderation.model.EventState;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByState(EventState state);

    Optional<Event> findByIdAndState(Long id, EventState state);

    List<Event> findByInitiatorId(Long initiatorId);
}