package ru.practicum.ewm.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationRequestServiceImplTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipationRequestMapper requestMapper;

    @InjectMocks
    private ParticipationRequestServiceImpl participationRequestService;

    private User user;
    private Event event;
    private ParticipationRequest participationRequest;
    private ParticipationRequestDto requestDto; // Это поле используется в нескольких методах, оставляем как поле

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();

        User initiator = User.builder()
                .id(2L)
                .name("Initiator")
                .email("initiator@example.com")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        event = Event.builder()
                .id(10L)
                .title("Event Title")
                .annotation("Event Annotation")
                .category(category)
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .createdDate(LocalDateTime.now())
                .build();

        participationRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(1L)
                .status("PENDING")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterId(anyLong())).thenReturn(List.of(participationRequest));
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        List<ParticipationRequestDto> result = participationRequestService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestDto.getId(), result.get(0).getId());

        verify(userRepository, times(1)).findById(1L);
        verify(requestRepository, times(1)).findAllByRequesterId(1L);
    }

    @Test
    void getUserRequests_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationRequestService.getUserRequests(999L));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());

        verify(userRepository, times(1)).findById(999L);
        verify(requestRepository, never()).findAllByRequesterId(anyLong());
    }

    @Test
    void createParticipationRequest_ShouldCreateRequestSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        assertNotNull(result);
        assertEquals(requestDto.getId(), result.getId());
        assertEquals("PENDING", result.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
        verify(requestMapper, times(1)).toDto(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_ByInitiator_ShouldThrowConflictException() {
        event.setInitiator(user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Инициатор события не может подать заявку на участие", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_ForUnpublishedEvent_ShouldThrowConflictException() {
        event.setState(EventState.PENDING);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Нельзя участвовать в неопубликованном событии", exception.getMessage());

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenParticipantLimitReached_ShouldThrowConflictException() {
        event.setParticipantLimit(1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(1L);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Достигнут лимит участников события", exception.getMessage());

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenRequestAlreadyExists_ShouldThrowConflictException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong()))
                .thenReturn(Optional.of(participationRequest));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Запрос на участие уже существует", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_ShouldCancelRequestSuccessfully() {
        participationRequest.setRequester(user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(participationRequest));
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        ParticipationRequestDto result = participationRequestService.cancelRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(RequestStatus.CANCELED, participationRequest.getStatus());
        assertEquals(requestDto.getId(), result.getId());

        verify(requestRepository, times(1)).save(participationRequest);
        verify(requestMapper, times(1)).toDto(participationRequest);
    }

    @Test
    void cancelRequest_ForNonOwnedRequest_ShouldThrowConflictException() {
        User anotherUser = User.builder().id(999L).build();
        participationRequest.setRequester(anotherUser);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(participationRequest));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.cancelRequest(1L, 1L));

        assertEquals("Запрос не принадлежит пользователю", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_WhenRequestNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationRequestService.cancelRequest(1L, 999L));

        assertEquals("Запрос на участие с id=999 не найден", exception.getMessage());

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenNoModerationRequired_ShouldAutoConfirm() {
        // Устанавливаем, что модерация не требуется
        event.setRequestModeration(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        assertNotNull(result);
        // Проверяем, что запрос автоматически подтвержден
        assertEquals(RequestStatus.CONFIRMED, participationRequest.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenNoParticipantLimit_ShouldAutoConfirm() {
        // Устанавливаем лимит участников = 0 (без ограничений)
        event.setParticipantLimit(0);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(participationRequest);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        assertNotNull(result);
        // Проверяем, что запрос автоматически подтвержден
        assertEquals(RequestStatus.CONFIRMED, participationRequest.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }
}