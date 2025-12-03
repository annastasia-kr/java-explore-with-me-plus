package ru.practicum.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.enumeration.StateEvent;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        imports = {LocalDateTime.class, StateEvent.class},
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "location", target = "location")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "participantLimit", expression = "java(newEventDto.getParticipantLimit() == null ? 0L : newEventDto.getParticipantLimit())")
    @Mapping(target = "requestModeration", expression = "java(newEventDto.getRequestModeration() == null ? true : newEventDto.getRequestModeration())")
    Event toEvent(NewEventDto newEventDto, Category category, User initiator, Location location);

    @Mapping(source = "event.category", target = "category")
    @Mapping(source = "event.initiator", target = "initiator")
    @Mapping(source = "event.location", target = "location")
    @Mapping(source = "confirmedRequests", target = "confirmedRequests")
    @Mapping(source = "views", target = "views")
    EventDto toEventDto(Event event, Long confirmedRequests, Long views);

    @Mapping(source = "category", target = "category")
    @Mapping(source = "confirmedRequests", target = "confirmedRequests")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "annotation", target = "annotation")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "eventDate", target = "eventDate")
    @Mapping(source = "paid", target = "paid")
    @Mapping(source = "participantLimit", target = "participantLimit")
    @Mapping(source = "views", target = "views")
    EventShortDto toEventShortDto(Event event);

}
