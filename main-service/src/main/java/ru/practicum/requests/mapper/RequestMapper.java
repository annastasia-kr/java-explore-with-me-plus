package ru.practicum.requests.mapper;

import org.mapstruct.Mapper;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    RequestDto toRequestDto(Request request);
}
