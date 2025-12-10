package ru.practicum.explorewithme.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;
import ru.practicum.explorewithme.event.dto.EventDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CommentService commentService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Существующие методы...

    // Пункт 7: Получение списка событий с комментариями
    public List<EventShortDto> getEventsWithComments(String text, List<Long> categories, Boolean paid,
                                                     String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                     String sort, Integer from, Integer size) {

        log.info("Getting events with comments");

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findPublishedEvents(
                text, categories, paid, start, end, onlyAvailable, pageable);

        // Получаем ID всех событий
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        // Получаем APPROVED комментарии для всех событий
        Map<Long, List<CommentDto>> commentsByEvent = commentService.getApprovedCommentsForEvents(eventIds);

        // Маппим в DTO и добавляем комментарии
        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    List<CommentDto> comments = commentsByEvent.getOrDefault(event.getId(), List.of());
                    dto.setComments(comments);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Пункт 8: Получение конкретного события с комментариями
    public EventDto getEventWithComments(Long eventId) {
        log.info("Getting event {} with comments", eventId);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        EventDto eventDto = eventMapper.toDto(event);

        // Получаем APPROVED комментарии для этого события
        List<CommentDto> comments = commentService.getApprovedCommentsForEvent(eventId);
        eventDto.setComments(comments);

        return eventDto;
    }
}