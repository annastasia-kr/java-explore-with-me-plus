package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.ParticipationRequestService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository; // Используем EventRepository
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя с ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие: userId={}, eventId={}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId) // Используем EventRepository
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        validateRequestCreation(userId, event);

        requestRepository.findByEventIdAndRequesterId(eventId, userId)
                .ifPresent(request -> {
                    throw new ConflictException("Запрос на участие уже существует");
                });

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Запрос на участие создан с ID: {}", savedRequest.getId());

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса: userId={}, requestId={}", userId, requestId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие с id=" + requestId + " не найден"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Запрос не принадлежит пользователю");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);
        log.info("Запрос на участие с ID {} отменен", requestId);

        return requestMapper.toDto(updatedRequest);
    }

    private void validateRequestCreation(Long userId, Event event) {
        // Используем метод getInitiator() для проверки
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getParticipantLimit() > 0) {
            // Используем метод countConfirmedRequests из репозитория
            Long confirmedRequests = eventRepository.countConfirmedRequests(event.getId());
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }
    }
}