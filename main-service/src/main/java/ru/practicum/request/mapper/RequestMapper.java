package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

@UtilityClass
public class RequestMapper {

    public static RequestDto toRequestDto(Request request) {

        RequestDto requestDto = new RequestDto(request.getId(), request.getCreated(), request.getRequester().getId(),
                request.getEvent().getId(), request.getStatus());

        return requestDto;
    }
}
