package ru.practicum.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.events.model.Event;
import ru.practicum.events.dto.EventShortDto;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "pinned", defaultValue = "false")
    Compilation toCompilation(NewCompilationDto compilation);

    @Mapping(target = "events", ignore = true)
    CompilationDto toCompilationDto(Compilation compilation);

    EventShortDto toEventShortDto(Event event);

    Collection<EventShortDto> toEventShortDtoCollection(List<Event> events);
}
