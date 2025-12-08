package ru.practicum.requests.controller;

import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateRequestController {

    private final RequestService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests - получение запросов пользователя", userId);
        return service.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(
            @PathVariable @Positive Long userId,
            @RequestParam @NotNull @Positive Long eventId) {
        log.info("POST /users/{}/requests - создание запроса на участие в событии {}", userId, eventId);
        return service.create(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - отмена запроса", userId, requestId);
        return service.cancelRequest(userId, requestId);
    }
}