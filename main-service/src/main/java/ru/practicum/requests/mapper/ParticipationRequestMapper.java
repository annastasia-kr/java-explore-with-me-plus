package ru.practicum.requests.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.model.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "created", source = "createdDate")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);
}