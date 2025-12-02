package ru.practicum.ewm.controller.privateapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.ParticipationRequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
@SuppressWarnings("unused")
public class PrivateParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("GET /users/{}/requests - получение запросов пользователя", userId);
        List<ParticipationRequestDto> requests = participationRequestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(
            @PathVariable Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("POST /users/{}/requests - создание запроса на участие в событии {}", userId, eventId);
        ParticipationRequestDto requestDto = participationRequestService.createParticipationRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestDto);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - отмена запроса", userId, requestId);
        ParticipationRequestDto requestDto = participationRequestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(requestDto);
    }
}