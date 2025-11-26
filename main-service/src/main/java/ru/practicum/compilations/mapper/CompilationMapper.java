package ru.practicum.compilations.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.model.Compilation;

@UtilityClass
public class CompilationMapper {
    public static Compilation toCompilation(CompilationDto dto) {
        Compilation o = new Compilation();
        o.setTitle(dto.getTitle());
        o.setPinned(dto.getPinned());
        return o;
    }

    public static CompilationDto toCompilationDto(Compilation o) {
        CompilationDto dto = new CompilationDto();
        return dto;
    }
}
