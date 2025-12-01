package ru.practicum.events.service;

import ru.practicum.events.dto.*;
import ru.practicum.events.model.enumeration.Sort;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.EventRequestStatusUpdateDto;
import ru.practicum.request.dto.RequestDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventService {

    Collection<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventDto createEvent(Long userId, NewEventDto newEventDto);

    EventDto getEventById(Long userId, Long eventId);

    EventDto updateEventByUser(Long userId, Long eventId, UpdateEventDtoUserRequest updateEventDtoUserRequest);

    Collection<RequestDto> getRequestsByUserIdAndEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateDto eventRequestStatusUpdateDto);

    Collection<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Integer from, Integer size);

    EventDto updateEventByAdmin(Long eventId, UpdateEventDtoAdminRequest updateEventDtoAdminRequest);

    EventDto getEvent(Long id);

    Collection<EventDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, Sort sort, Integer from, Integer size);
}
