package ru.practicum.requests.service;

import ru.practicum.requests.dto.ParticipationRequestDto;
import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);
    ParticipationRequestDto createParticipationRequest(Long userId, Long eventId);
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}