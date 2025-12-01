package ru.practicum.events.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.model.Category;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.enumeration.StateEvent;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;
import static ru.practicum.events.mapper.LocationMapper.toLocationDto;
import static ru.practicum.user.UserMapper.toUserShortDto;

@UtilityClass
public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto, Category category, User user, Location location) {

        Event event = new Event();

        event.setAnnotation(newEventDto.getAnnotation());
        event.setTitle(newEventDto.getTitle());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);
        event.setEventDate(newEventDto.getEventDate());
        event.setInitiator(user);
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit() == null ? 0L : newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration() == null ? true : newEventDto.getRequestModeration());
        event.setLocation(location);
        event.setState(StateEvent.PENDING);
        event.setViews(0L);

        return event;
    }

    public static EventDto toEventDto(Event event) {

        EventDto eventDto = new EventDto();

        eventDto.setId(event.getId());
        eventDto.setAnnotation(event.getAnnotation());
        eventDto.setTitle(event.getTitle());
        eventDto.setCategory(toCategoryDto(event.getCategory()));
        eventDto.setDescription(event.getDescription());
        eventDto.setCreatedOn(event.getCreatedOn());
        eventDto.setConfirmedRequests(event.getConfirmedRequests());
        eventDto.setEventDate(event.getEventDate());
        eventDto.setInitiator(toUserShortDto(event.getInitiator()));
        eventDto.setPaid(event.getPaid());
        eventDto.setParticipantLimit(event.getParticipantLimit());
        eventDto.setRequestModeration(event.getRequestModeration());
        eventDto.setLocation(toLocationDto(event.getLocation()));
        eventDto.setState(event.getState());
        eventDto.setViews(event.getViews());

        return eventDto;
    }

    public static EventShortDto toEventShortDto(Event event) {

        EventShortDto eventShortDto = new EventShortDto();

        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setCategory(toCategoryDto(event.getCategory()));
        eventShortDto.setConfirmedRequests(event.getConfirmedRequests());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setInitiator(toUserShortDto(event.getInitiator()));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setParticipantLimit(event.getParticipantLimit());
        eventShortDto.setViews(event.getViews());

        return eventShortDto;

    }

}
