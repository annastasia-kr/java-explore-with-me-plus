package ru.practicum.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(CompilationDto compilation);

    @Mapping(target = "id", ignore = true)
    Compilation toCompilation(NewCompilationDto compilation);

    @Mapping(target = "events", source = "events")
    CompilationDto toCompilationDto(Compilation compilation);
}
