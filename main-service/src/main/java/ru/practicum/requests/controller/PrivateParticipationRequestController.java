package ru.practicum.requests.controller;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests - получение запросов пользователя", userId);
        return participationRequestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createParticipationRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @NotNull @Positive Long eventId) {
        log.info("POST /users/{}/requests - создание запроса на участие в событии {}", userId, eventId);
        return participationRequestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - отмена запроса", userId, requestId);
        return participationRequestService.cancelRequest(userId, requestId);
    }
}