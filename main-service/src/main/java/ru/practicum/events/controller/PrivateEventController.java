package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventDtoUserRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.events.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.practicum.requests.dto.EventRequestStatusUpdateDto;
import ru.practicum.requests.dto.RequestDto;

import java.util.Collection;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<EventShortDto> getEventsByUserId(@PathVariable Long userId,
                                                       @RequestParam(defaultValue = "0", required = false) Integer from,
                                                       @RequestParam(defaultValue = "10", required = false) Integer size) {
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable Long userId,
                                @RequestBody @NotNull @Valid NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventById(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return eventService.getEventById(userId, eventId);
    }


    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEventByUser(@PathVariable Long userId,
                                      @PathVariable Long eventId,
                                      @RequestBody @NotNull @Valid UpdateEventDtoUserRequest updateEventDtoUserRequest) {
        return eventService.updateEventByUser(userId, eventId, updateEventDtoUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public Collection<RequestDto> getRequestsByUserIdAndEventId(@PathVariable Long userId,
                                                                @PathVariable Long eventId) {
        return eventService.getRequestsByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId,
                                                       @PathVariable Long eventId,
                                                       @RequestBody EventRequestStatusUpdateDto eventRequestStatusUpdateDto) {
        return eventService.updateRequestStatus(userId, eventId, eventRequestStatusUpdateDto);
    }
}
