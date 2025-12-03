package ru.practicum.requests.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.categories.model.Category;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.model.Event;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.requests.mapper.ParticipationRequestMapper;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.respository.ParticipationRequestRepository;
import ru.practicum.users.enums.RequestStatus;
import ru.practicum.users.model.User;
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
                user = new User();
                user.setId(1L);
                user.setName("User");
                user.setEmail("user@example.com");

                User initiator = new User();
                initiator.setId(2L);
                initiator.setName("Initiator");
                initiator.setEmail("initiator@example.com");

                Category category = new Category();
                category.setId(1L);
                category.setName("Category");

                event = new Event();
                event.setId(10L);
                event.setTitle("Event Title");
                event.setAnnotation("Event Annotation");
                event.setCategory(category);
                event.setEventDate(LocalDateTime.now().plusDays(1));
                event.setInitiator(initiator); // Важно: initiator отличается от user
                event.setPaid(false);
                event.setParticipantLimit(10L);
                event.setRequestModeration(true);
                event.setState(StateEvent.PUBLISHED);
                event.setCreatedOn(LocalDateTime.now());

                participationRequest = new ParticipationRequest();
                participationRequest.setId(1L);
                participationRequest.setEvent(event);
                participationRequest.setRequester(user);
                participationRequest.setStatus(RequestStatus.PENDING);
                participationRequest.setCreatedDate(LocalDateTime.now());

                requestDto = new ParticipationRequestDto();
                requestDto.setId(1L);
                requestDto.setEvent(10L);
                requestDto.setRequester(1L);
                requestDto.setStatus("PENDING");
                requestDto.setCreated(LocalDateTime.now());
        }

        @Test
        void createParticipationRequest_ShouldCreateRequestSuccessfully() {
                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());

                // Мок для сохранения - должен вернуть request с PENDING статусом
                ParticipationRequest pendingRequest = new ParticipationRequest();
                pendingRequest.setId(1L);
                pendingRequest.setEvent(event);
                pendingRequest.setRequester(user);
                pendingRequest.setStatus(RequestStatus.PENDING); // PENDING, т.к. requestModeration = true
                pendingRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(pendingRequest);
                when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(requestDto);

                ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

                assertNotNull(result);
                assertEquals("PENDING", result.getStatus());

                verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
                verify(requestMapper, times(1)).toDto(any(ParticipationRequest.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
        }

        @Test
        void createParticipationRequest_WhenNoModerationRequired_ShouldAutoConfirm() {
                // Arrange
                event.setRequestModeration(false); // Модерация не требуется

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());

                // Создаем CONFIRMED запрос для мока
                ParticipationRequest confirmedRequest = new ParticipationRequest();
                confirmedRequest.setId(1L);
                confirmedRequest.setEvent(event);
                confirmedRequest.setRequester(user);
                confirmedRequest.setStatus(RequestStatus.CONFIRMED); // Должен быть CONFIRMED
                confirmedRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(confirmedRequest);

                ParticipationRequestDto confirmedDto = new ParticipationRequestDto();
                confirmedDto.setId(1L);
                confirmedDto.setEvent(10L);
                confirmedDto.setRequester(1L);
                confirmedDto.setStatus("CONFIRMED"); // Должен быть CONFIRMED
                confirmedDto.setCreated(LocalDateTime.now());

                when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(confirmedDto);

                // Act
                ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

                // Assert
                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
                verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
        }

        @Test
        void createParticipationRequest_WhenNoParticipantLimit_ShouldAutoConfirm() {
                // Arrange
                event.setParticipantLimit(0L); // Без лимита участников

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.findByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(Optional.empty());

                // Создаем CONFIRMED запрос для мока
                ParticipationRequest confirmedRequest = new ParticipationRequest();
                confirmedRequest.setId(1L);
                confirmedRequest.setEvent(event);
                confirmedRequest.setRequester(user);
                confirmedRequest.setStatus(RequestStatus.CONFIRMED); // Должен быть CONFIRMED
                confirmedRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(confirmedRequest);

                ParticipationRequestDto confirmedDto = new ParticipationRequestDto();
                confirmedDto.setId(1L);
                confirmedDto.setEvent(10L);
                confirmedDto.setRequester(1L);
                confirmedDto.setStatus("CONFIRMED"); // Должен быть CONFIRMED
                confirmedDto.setCreated(LocalDateTime.now());

                when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(confirmedDto);

                // Act
                ParticipationRequestDto result = participationRequestService.createParticipationRequest(1L, 10L);

                // Assert
                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
                verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
                verify(requestRepository, never()).countConfirmedRequests(anyLong());
        }

        @Test
        void createParticipationRequest_WhenParticipantLimitReached_ShouldThrowConflictException() {
                // Arrange
                event.setParticipantLimit(1L); // Лимит 1 участник

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(1L); // Уже 1 подтвержденный

                // Act & Assert
                ConflictException exception = assertThrows(ConflictException.class,
                                () -> participationRequestService.createParticipationRequest(1L, 10L));

                assertEquals("Достигнут лимит участников события", exception.getMessage());
                verify(requestRepository, never()).save(any(ParticipationRequest.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
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
                verify(requestRepository, never()).findByEventIdAndRequesterId(anyLong(), anyLong());
                verify(requestRepository, never()).countConfirmedRequests(anyLong());
        }

        @Test
        void cancelRequest_ShouldCancelRequestSuccessfully() {
                // Arrange
                participationRequest.setRequester(user); // Устанавливаем, что запрос принадлежит user

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(requestRepository.findById(anyLong())).thenReturn(Optional.of(participationRequest));

                // Создаем canceled request для мока
                ParticipationRequest canceledRequest = new ParticipationRequest();
                canceledRequest.setId(1L);
                canceledRequest.setEvent(event);
                canceledRequest.setRequester(user);
                canceledRequest.setStatus(RequestStatus.CANCELED);
                canceledRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(canceledRequest);

                ParticipationRequestDto canceledDto = new ParticipationRequestDto();
                canceledDto.setId(1L);
                canceledDto.setEvent(10L);
                canceledDto.setRequester(1L);
                canceledDto.setStatus("CANCELED");
                canceledDto.setCreated(LocalDateTime.now());

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