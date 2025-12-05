package ru.practicum.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.UpdateEventDtoAdminRequest;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventDto> getEventsByAdmin(@RequestParam(required = false) List<Long> users,
                                                 @RequestParam(required = false) List<StateEvent> states,
                                                 @RequestParam(required = false) List<Long> categories,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEvent(@PathVariable @Positive Long eventId,
                                @Valid @RequestBody UpdateEventDtoAdminRequest updateEventDtoAdminRequest) {

        return eventService.updateEventByAdmin(eventId, updateEventDtoAdminRequest);
    }
}
