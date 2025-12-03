package ru.practicum.requests.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.requests.respository.ParticipationRequestRepository;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.requests.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
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

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        validateRequestCreation(userId, event);

        requestRepository.findByEventIdAndRequesterId(eventId, userId)
                .ifPresent(request -> {
                    throw new ConflictException("Запрос на участие уже существует");
                });

        // Создаем запрос и сразу устанавливаем дату создания
        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .createdDate(LocalDateTime.now()) // Устанавливаем дату здесь
                .build();

        // Проверяем условия для авто-подтверждения
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
        // Проверяем, что инициатор не подает заявку на свое же событие
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на участие");
        }

        // Проверяем, что событие опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        // Проверяем лимит участников
        if (event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countConfirmedRequests(event.getId());
            if (confirmedRequests != null && confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }
    }
}