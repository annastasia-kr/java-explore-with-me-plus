package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.ParticipationRequestDto;
import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);
    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}