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
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.requests.enums.RequestStatus;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

        @Mock
        private RequestRepository requestRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private EventRepository eventRepository;

        @Mock
        private RequestMapper requestMapper;

        @InjectMocks
        private RequestServiceImpl requestService;

        private User user;
        private Event event;
        private Request request;
        private RequestDto requestDto;

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

                request = new Request();
                request.setId(1L);
                request.setEvent(event);
                request.setRequester(user);
                request.setStatus(RequestStatus.PENDING);
                request.setCreatedDate(LocalDateTime.now());

                requestDto = new RequestDto();
                requestDto.setId(1L);
                requestDto.setEvent(10L);
                requestDto.setRequester(1L);
                requestDto.setStatus("PENDING");
                requestDto.setCreated(LocalDateTime.now());
        }

        @Test
        void createRequest_ShouldCreateRequestSuccessfully() {
                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);

                // Мок для сохранения - должен вернуть request с PENDING статусом
                Request pendingRequest = new Request();
                pendingRequest.setId(1L);
                pendingRequest.setEvent(event);
                pendingRequest.setRequester(user);
                pendingRequest.setStatus(RequestStatus.PENDING); // PENDING, т.к. requestModeration = true
                pendingRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(Request.class))).thenReturn(pendingRequest);
                when(requestMapper.toRequestDto(any(Request.class))).thenReturn(requestDto);

                RequestDto result = requestService.create(1L, 10L);

                assertNotNull(result);
                assertEquals("PENDING", result.getStatus());

                verify(requestRepository, times(1)).save(any(Request.class));
                verify(requestMapper, times(1)).toRequestDto(any(Request.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
        }

        @Test
        void createRequest_WhenNoModerationRequired_ShouldAutoConfirm() {
                // Arrange
                event.setRequestModeration(false); // Модерация не требуется

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);

                // Создаем CONFIRMED запрос для мока
                Request confirmedRequest = new Request();
                confirmedRequest.setId(1L);
                confirmedRequest.setEvent(event);
                confirmedRequest.setRequester(user);
                confirmedRequest.setStatus(RequestStatus.CONFIRMED); // Должен быть CONFIRMED
                confirmedRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(Request.class))).thenReturn(confirmedRequest);

                RequestDto confirmedDto = new RequestDto();
                confirmedDto.setId(1L);
                confirmedDto.setEvent(10L);
                confirmedDto.setRequester(1L);
                confirmedDto.setStatus("CONFIRMED"); // Должен быть CONFIRMED
                confirmedDto.setCreated(LocalDateTime.now());

                when(requestMapper.toRequestDto(any(Request.class))).thenReturn(confirmedDto);

                // Act
                RequestDto result = requestService.create(1L, 10L);

                // Assert
                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
                verify(requestRepository, times(1)).save(any(Request.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
        }

        @Test
        void createRequest_WhenNoParticipantLimit_ShouldAutoConfirm() {
                // Arrange
                event.setParticipantLimit(0L); // Без лимита участников

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);

                // Создаем CONFIRMED запрос для мока
                Request confirmedRequest = new Request();
                confirmedRequest.setId(1L);
                confirmedRequest.setEvent(event);
                confirmedRequest.setRequester(user);
                confirmedRequest.setStatus(RequestStatus.CONFIRMED); // Должен быть CONFIRMED
                confirmedRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(Request.class))).thenReturn(confirmedRequest);

                RequestDto confirmedDto = new RequestDto();
                confirmedDto.setId(1L);
                confirmedDto.setEvent(10L);
                confirmedDto.setRequester(1L);
                confirmedDto.setStatus("CONFIRMED"); // Должен быть CONFIRMED
                confirmedDto.setCreated(LocalDateTime.now());

                when(requestMapper.toRequestDto(any(Request.class))).thenReturn(confirmedDto);

                // Act
                RequestDto result = requestService.create(1L, 10L);

                // Assert
                assertNotNull(result);
                assertEquals("CONFIRMED", result.getStatus()); // Проверяем CONFIRMED статус
                verify(requestRepository, times(1)).save(any(Request.class));
                verify(requestRepository, never()).countConfirmedRequests(anyLong());
        }

        @Test
        void createRequest_WhenParticipantLimitReached_ShouldThrowConflictException() {
                // Arrange
                event.setParticipantLimit(1L); // Лимит 1 участник

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
                when(requestRepository.countConfirmedRequests(anyLong())).thenReturn(1L); // Уже 1 подтвержденный

                // Act & Assert
                ConflictException exception = assertThrows(ConflictException.class,
                                () -> requestService.create(1L, 10L));

                assertEquals("Достигнут лимит участников события", exception.getMessage());
                verify(requestRepository, never()).save(any(Request.class));
                verify(requestRepository, times(1)).countConfirmedRequests(anyLong());
        }

        @Test
        void createRequest_ByInitiator_ShouldThrowConflictException() {
                // Arrange: делаем так, чтобы user был инициатором события
                event.setInitiator(user);

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

                // Act & Assert
                ConflictException exception = assertThrows(ConflictException.class,
                                () -> requestService.create(1L, 10L));

                assertEquals("Инициатор события не может подать заявку на участие", exception.getMessage());
                verify(requestRepository, never()).save(any(Request.class));
                verify(requestRepository, never()).existsByEventIdAndRequesterId(anyLong(), anyLong());
                verify(requestRepository, never()).countConfirmedRequests(anyLong());
        }

        @Test
        void cancelRequest_ShouldCancelRequestSuccessfully() {
                // Arrange
                request.setRequester(user); // Устанавливаем, что запрос принадлежит user

                when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));

                // Создаем canceled request для мока
                Request canceledRequest = new Request();
                canceledRequest.setId(1L);
                canceledRequest.setEvent(event);
                canceledRequest.setRequester(user);
                canceledRequest.setStatus(RequestStatus.CANCELED);
                canceledRequest.setCreatedDate(LocalDateTime.now());

                when(requestRepository.save(any(Request.class))).thenReturn(canceledRequest);

                RequestDto canceledDto = new RequestDto();
                canceledDto.setId(1L);
                canceledDto.setEvent(10L);
                canceledDto.setRequester(1L);
                canceledDto.setStatus("CANCELED");
                canceledDto.setCreated(LocalDateTime.now());

                when(requestMapper.toRequestDto(any(Request.class))).thenReturn(canceledDto);

                // Act
                RequestDto result = requestService.cancelRequest(1L, 1L);

                // Assert
                assertNotNull(result);
                assertEquals("CANCELED", result.getStatus());
                verify(requestRepository, times(1)).save(any(Request.class));
                verify(requestMapper, times(1)).toRequestDto(any(Request.class));
        }
}