package ru.practicum.compilations.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.model.Compilation;

@UtilityClass
public class CompilationMapper {
    public static Compilation toCompilation(CompilationDto o) {
        Compilation compilation = new Compilation();
        return compilation;
    }


}
