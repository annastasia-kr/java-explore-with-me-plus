package ru.practicum.events.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.StatsDto;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.dto.*;
import ru.practicum.events.enums.StateActionUser;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.mapper.LocationMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.events.enums.Sort;
import ru.practicum.events.enums.StateActionAdmin;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.exception.*;
import ru.practicum.exception.IllegalStateException;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.requests.dto.EventRequestStatusUpdateDto;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.Request;
import ru.practicum.requests.enums.RequestStatus;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String MAIN_SERVICE = "main-service";
    private static final String URI = "/events/";

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EntityManager entityManager;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final RequestMapper requestMapper;

    @Override
    public Collection<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        Pageable page = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, page).stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional
    public EventDto createEvent(Long userId, NewEventDto newEventDto) {
        User findedUser = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        Category findedCategory = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                () -> new NotFoundException("Category not found"));
        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("The event date must exceed the current timestamp + 2H");
        }
        Event createdEvent = eventMapper.toEvent(newEventDto, findedCategory, findedUser,
                getEventLocation(newEventDto.getLocation()));
        return eventMapper.toEventDto(eventRepository.save(createdEvent), 0L, 0L);

    }

    @Override
    public EventDto getEventById(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        return eventMapper.toEventDto(event, confirmedRequests, 0L);

    }

    @Override
    @Transactional
    public EventDto updateEventByUser(Long userId, Long eventId, UpdateEventDtoUserRequest updateEventDtoUserRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (event.getState() == StateEvent.PUBLISHED) {
            throw new DataConflictException("Operation is not permitted for a published event");
        }

        if (updateEventDtoUserRequest.getStateAction() != null) {
            if (updateEventDtoUserRequest.getStateAction() == StateActionUser.SEND_TO_REVIEW) {
                event.setState(StateEvent.PENDING);
            } else if (updateEventDtoUserRequest.getStateAction() == StateActionUser.CANCEL_REVIEW) {
                event.setState(StateEvent.CANCELED);
            }
        }

        if (updateEventDtoUserRequest.getAnnotation() != null && !updateEventDtoUserRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventDtoUserRequest.getAnnotation());
        }
        if (updateEventDtoUserRequest.getDescription() != null
                && !updateEventDtoUserRequest.getDescription().isBlank()) {
            event.setDescription(updateEventDtoUserRequest.getDescription());
        }
        if (updateEventDtoUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventDtoUserRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category not found")));
        }
        if (updateEventDtoUserRequest.getEventDate() != null) {
            if (!updateEventDtoUserRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("The event date must exceed the current timestamp + 2H");
            }
            event.setEventDate(updateEventDtoUserRequest.getEventDate());
        }
        if (updateEventDtoUserRequest.getLocation() != null && updateEventDtoUserRequest.getLocation().getLat() != null
                && updateEventDtoUserRequest.getLocation().getLon() != null) {
            event.setLocation(getEventLocation(updateEventDtoUserRequest.getLocation()));
        }
        if (updateEventDtoUserRequest.getPaid() != null) {
            event.setPaid(updateEventDtoUserRequest.getPaid());
        }
        if (updateEventDtoUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDtoUserRequest.getParticipantLimit());
        }
        if (updateEventDtoUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDtoUserRequest.getRequestModeration());
        }
        if (updateEventDtoUserRequest.getTitle() != null) {
            event.setTitle(updateEventDtoUserRequest.getTitle());
        }
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        return eventMapper.toEventDto(eventRepository.save(event), confirmedRequests, 0L);
    }

    @Override
    public Collection<RequestDto> getRequestsByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedForUserException("Access denied: User is not an initiator");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
            EventRequestStatusUpdateDto eventRequestStatusUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedForUserException("Access denied: User is not an initiator");
        }
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new IllegalStateException("Application confirmation is not required");
        }
        List<Request> requestsToStatusUpdate = requestRepository
                .findAllByIdIn(eventRequestStatusUpdateDto.getRequestIds());
        if (requestsToStatusUpdate.stream()
                .anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING))) {
            throw new DataConflictException("Applications must be in status \"PENDING\"");
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        RequestStatus updateStatus = eventRequestStatusUpdateDto.getStatus();

        // Проверяем, не превысит ли подтверждение лимит участников
        if (updateStatus == RequestStatus.CONFIRMED) {
            Long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedRequestsCount + requestsToStatusUpdate.size() > event.getParticipantLimit()) {
                throw new DataConflictException("Application limit exceeded - confirmation not allowed");
            }
        }

        if (updateStatus == RequestStatus.REJECTED) {
            for (Request request : requestsToStatusUpdate) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        } else if (updateStatus == RequestStatus.CONFIRMED) {
            for (Request request : requestsToStatusUpdate) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
            }

            // Если после подтверждения будет достигнут лимит, отклоняем все остальные
            // pending заявки
            Long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedRequestsCount + requestsToStatusUpdate.size() >= event.getParticipantLimit()) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId,
                        RequestStatus.PENDING);
                for (Request pendingRequest : pendingRequests) {
                    pendingRequest.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(pendingRequest);
                }
            }
        }

        requestRepository.saveAll(confirmedRequests);
        requestRepository.saveAll(rejectedRequests);

        return new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(requestMapper::toRequestDto)
                        .toList(),
                rejectedRequests.stream()
                        .map(requestMapper::toRequestDto)
                        .toList());
    }

    @Override
    public Collection<EventDto> getEventsByAdmin(List<Long> users, List<StateEvent> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("RangeStart is not earlier than rangeEnd");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();
        applyUserFilter(predicates, root, users);
        applyStateFilter(predicates, root, states);
        applyCategoryFilter(predicates, root, categories);
        applyDateRangeFilter(predicates, cb, root, rangeStart, rangeEnd);

        if (!predicates.isEmpty()) {
            criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        List<Event> events = typedQuery.getResultList();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequestsMap = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        EventResult::getEventId,
                        EventResult::getCount));

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = (confirmedRequestsMap != null) ? confirmedRequestsMap.getOrDefault(event.getId(), 0L) : 0L;
                    return eventMapper.toEventDto(event, confirmedRequests, 0L);
                })
                .toList();
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, UpdateEventDtoAdminRequest updateEventDtoAdminRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));

        if (event.getState() != StateEvent.PENDING) {
            throw new DataConflictException("To publish an event, it must first be in the pending publication status");
        }

        if (updateEventDtoAdminRequest.getStateAction() != null) {
            if (updateEventDtoAdminRequest.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(StateEvent.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventDtoAdminRequest.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
                if (event.getState().equals(StateEvent.PUBLISHED)) {
                    throw new DataConflictException("An event can only be rejected if it has not yet been published");
                }
                event.setState(StateEvent.CANCELED);
            }
        }

        if (updateEventDtoAdminRequest.getEventDate() != null) {
            if (updateEventDtoAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("The event date must exceed the current timestamp + 1H");
            } else {
                event.setEventDate(updateEventDtoAdminRequest.getEventDate());
            }
        }
        if (updateEventDtoAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventDtoAdminRequest.getAnnotation());
        }
        if (updateEventDtoAdminRequest.getDescription() != null) {
            event.setDescription(updateEventDtoAdminRequest.getDescription());
        }
        if (updateEventDtoAdminRequest.getPaid() != null) {
            event.setPaid(updateEventDtoAdminRequest.getPaid());
        }
        if (updateEventDtoAdminRequest.getTitle() != null) {
            event.setTitle(updateEventDtoAdminRequest.getTitle());
        }
        if (updateEventDtoAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventDtoAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found")));
        }
        if (updateEventDtoAdminRequest.getLocation() != null) {
            event.setLocation(getEventLocation(updateEventDtoAdminRequest.getLocation()));
        }
        if (updateEventDtoAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDtoAdminRequest.getParticipantLimit());
        }
        if (updateEventDtoAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDtoAdminRequest.getRequestModeration());
        }
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED);
        return eventMapper.toEventDto(eventRepository.save(event), confirmedRequests, 0L);
    }

    @Override
    public Collection<EventDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, Sort sort, Integer from, Integer size, HttpServletRequest httpServletRequest) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("RangeStart is not earlier than rangeEnd");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        applyTextFilter(predicates, cb, root, text);
        applyCategoryFilter(predicates, root, categories);
        applyPaidFilter(predicates, cb, root, paid);
        applyDateRangeFilter(predicates, cb, root, rangeStart, rangeEnd);
        applyStateFilter(predicates, root, List.of(StateEvent.PUBLISHED));

        if (!predicates.isEmpty()) {
            criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        if (sort != null) {
            applySortValue(criteriaQuery, cb, root, sort);
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        List<Event> events = typedQuery.getResultList();

        statsClient.saveHit(MAIN_SERVICE, httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr());

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequestsMap = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        EventResult::getEventId,
                        EventResult::getCount));

        LocalDateTime minStartDate = events.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo) // минимум
                .orElse(LocalDateTime.now());

        List<StatsDto> statistics = statsClient.getStats(minStartDate, LocalDateTime.now(),
               (eventIds.stream()
                                .map(id -> URI + id)
                                .toList()), true).stream()
                        .toList();

        Map<String, Long> hits = statistics.stream()
                .collect(Collectors.toMap(
                        StatsDto::getUri,
                        StatsDto::getHits
                ));

        Map<Long, Long> eventsIdWithHits = eventIds.stream()
                .collect(Collectors.toMap(
                        num -> num,
                        num -> hits.getOrDefault(URI + num, 0L)
                ));

        return events.stream()
                .filter(event -> !onlyAvailable ||
                        event.getParticipantLimit() == 0 ||
                        ((confirmedRequestsMap != null) ? confirmedRequestsMap.getOrDefault(event.getId(), 0L) : 0L) < event.getParticipantLimit()
                )
                .map(event -> {
                    Long confirmedRequests = (confirmedRequestsMap != null) ? confirmedRequestsMap.getOrDefault(event.getId(), 0L) : 0L;
                    Long views = eventsIdWithHits.getOrDefault(event.getId(), 0L);

                    return eventMapper.toEventDto(event, confirmedRequests, views);
                })
                .toList();
    }

    @Override
    public EventDto getEvent(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new NotFoundException("Event must be published");
        }
        statsClient.saveHit(MAIN_SERVICE, httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr());

        LocalDateTime start = event.getPublishedOn() == null ? event.getCreatedOn() : event.getPublishedOn();

        eventRepository.save(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED);
        Long views = getEventViews(start, event.getId());
        return eventMapper.toEventDto(event, confirmedRequests, views);
    }

    private Location getEventLocation(LocationDto locationDto) {
        Optional<Location> location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        return location.orElseGet(() -> locationRepository.save(locationMapper.toLocation(locationDto, 0L)));
    }

    private void applyDateRangeFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root,
            LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"),
                Objects.requireNonNullElseGet(rangeStart, LocalDateTime::now)));
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
    }

    private void applyCategoryFilter(List<Predicate> predicates, Root<Event> root, List<Long> categories) {
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
    }

    private void applyStateFilter(List<Predicate> predicates, Root<Event> root,
            List<StateEvent> states) {
        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("state").in(states));
        }
    }

    private void applyUserFilter(List<Predicate> predicates, Root<Event> root, List<Long> users) {
        if (users != null && !users.isEmpty()) {
            predicates.add(root.get("initiator").get("id").in(users));
        }
    }

    private void applyTextFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, String text) {
        if (text != null && !text.isBlank()) {
            String searchText = "%" + text.toLowerCase() + "%";
            predicates.add(cb.or(cb.like(cb.lower(root.get("annotation")), searchText),
                    cb.like(cb.lower(root.get("description")), searchText)));
        }
    }

    private void applyPaidFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, Boolean paid) {
        if (paid != null) {
            predicates.add(cb.equal(root.get("paid"), paid));
        }
    }

    private void applySortValue(CriteriaQuery<Event> query, CriteriaBuilder cb, Root<Event> root, Sort sort) {
        if (sort != null) {
            query.orderBy(cb.desc(root.get(sort.getParamName())));
        }
    }

    private Long getEventViews(LocalDateTime createdOn, Long eventId) {
        List<StatsDto> stat = statsClient.getStats(createdOn, LocalDateTime.now(),
                List.of(URI + eventId), true);
        if (stat.isEmpty()) {
            return 0L;
        }
        return stat.get(0).getHits();
    }
}
