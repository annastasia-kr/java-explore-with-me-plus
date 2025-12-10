package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.event.dto.EventDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category", source = "category.id")
    @Mapping(target = "initiator", source = "initiator.id")
    @Mapping(target = "comments", ignore = true)
    EventDto toDto(Event event);

    @Mapping(target = "category", source = "category.id")
    @Mapping(target = "initiator", source = "initiator.id")
    @Mapping(target = "comments", ignore = true)
    EventShortDto toShortDto(Event event);
}