package ru.practicum.explorewithme.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.client.StatsClient;
import ru.practicum.explorewithme.event.dto.EventDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    // Пункт 7: Получение событий с комментариями
    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        log.info("GET /events with params");

        statsClient.saveHit(request);

        return eventService.getEventsWithComments(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size
        );
    }

    // Пункт 8: Получение конкретного события с комментариями
    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("GET /events/{}", eventId);

        statsClient.saveHit(request);

        return eventService.getEventWithComments(eventId);
    }
}