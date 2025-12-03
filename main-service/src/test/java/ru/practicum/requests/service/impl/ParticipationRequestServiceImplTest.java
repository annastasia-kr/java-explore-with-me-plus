package java.ru.practicum.requests.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.requests.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.requests.respository.ParticipationRequestRepository;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
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
    private ParticipationRequestDto requestDto;

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
                .initiator(initiator) // Важно: initiator отличается от user
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
    void createParticipationRequest_ShouldCreateRequestSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);

        // Мок для сохранения - должен вернуть request с PENDING статусом
        ParticipationRequest pendingRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING) // PENDING, т.к. requestModeration = true
                .createdDate(LocalDateTime.now())
                .build();

        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(pendingRequest);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
        verify(requestMapper, times(1)).toDto(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenNoModerationRequired_ShouldAutoConfirm() {
        // Arrange
        event.setRequestModeration(false); // Модерация не требуется

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);

        // Создаем CONFIRMED запрос для мока
        ParticipationRequest confirmedRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.CONFIRMED) // Должен быть CONFIRMED
                .createdDate(LocalDateTime.now())
                .build();

        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(confirmedRequest);

        ParticipationRequestDto confirmedDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(1L)
                .status("CONFIRMED") // Должен быть CONFIRMED
                .created(LocalDateTime.now())
                .build();

        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(confirmedDto);

        // Act
        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        // Assert
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenNoParticipantLimit_ShouldAutoConfirm() {
        // Arrange
        event.setParticipantLimit(0); // Без лимита участников

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(0L);

        // Создаем CONFIRMED запрос для мока
        ParticipationRequest confirmedRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.CONFIRMED) // Должен быть CONFIRMED
                .createdDate(LocalDateTime.now())
                .build();

        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(confirmedRequest);

        ParticipationRequestDto confirmedDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(1L)
                .status("CONFIRMED") // Должен быть CONFIRMED
                .created(LocalDateTime.now())
                .build();

        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(confirmedDto);

        // Act
        ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

        // Assert
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_WhenParticipantLimitReached_ShouldThrowConflictException() {
        // Arrange
        event.setParticipantLimit(1); // Лимит 1 участник

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(1L); // Уже 1 подтвержденный

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Достигнут лимит участников события", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void createParticipationRequest_ByInitiator_ShouldThrowConflictException() {
        // Arrange: делаем так, чтобы user был инициатором события
        event.setInitiator(user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> participationRequestService.createParticipationRequest(1L, 10L));

        assertEquals("Инициатор события не может подать заявку на участие", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_ShouldCancelRequestSuccessfully() {
        // Arrange
        participationRequest.setRequester(user); // Устанавливаем, что запрос принадлежит user

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(participationRequest));

        // Создаем canceled request для мока
        ParticipationRequest canceledRequest = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.CANCELED)
                .createdDate(LocalDateTime.now())
                .build();

        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(canceledRequest);

        ParticipationRequestDto canceledDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(1L)
                .status("CANCELED")
                .created(LocalDateTime.now())
                .build();

        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(canceledDto);

        // Act
        ParticipationRequestDto result = participationRequestService.cancelRequest(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELED", result.getStatus());
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
        verify(requestMapper, times(1)).toDto(any(ParticipationRequest.class));
    }
}