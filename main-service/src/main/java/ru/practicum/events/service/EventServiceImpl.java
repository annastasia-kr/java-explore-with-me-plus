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
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.events.dto.*;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Location;
import ru.practicum.events.model.enumeration.Sort;
import ru.practicum.events.model.enumeration.StateActionAdmin;
import ru.practicum.events.model.enumeration.StateEvent;
import ru.practicum.exception.*;
import ru.practicum.exception.IllegalStateException;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateDto;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.enumeration.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.events.mapper.EventMapper.*;
import static ru.practicum.events.mapper.LocationMapper.toLocation;
import static ru.practicum.request.mapper.RequestMapper.toRequestDto;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EntityManager entityManager;
    private final StatsClient statsClient;

    @Override
    public Collection<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        Pageable page = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, page).stream()
                .map(event -> toEventShortDto(event))
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
            new ValidationException("The event date must exceed the current timestamp + 2H");
        }
        Event createdEvent = toEvent(newEventDto, findedCategory, findedUser, getEventLocation(newEventDto.getLocation()));
        return toEventDto(eventRepository.save(createdEvent));

    }

    @Override
    public EventDto getEventById(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        return toEventDto(eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found")));

    }

    @Override
    @Transactional
    public EventDto updateEventByUser(Long userId, Long eventId, UpdateEventDtoUserRequest updateEventDtoUserRequest) {

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (event.getState() == StateEvent.PUBLISHED) {
            throw new DataConflictException("Operation is not permitted for a published event");
        }

        if (updateEventDtoUserRequest.getAnnotation() != null && !updateEventDtoUserRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventDtoUserRequest.getAnnotation());
        }
        if (updateEventDtoUserRequest.getDescription() != null && !updateEventDtoUserRequest.getDescription().isBlank()) {
            event.setDescription(updateEventDtoUserRequest.getDescription());
        }
        if (updateEventDtoUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventDtoUserRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Category not found")));
        }
        if (updateEventDtoUserRequest.getCategory() != null) {
            if (!updateEventDtoUserRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                new DataConflictException("The event date must exceed the current timestamp + 2H");
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
        return toEventDto(eventRepository.save(event));
    }

    @Override
    public Collection<RequestDto> getRequestsByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedForUserException("Access denied: User is not an initiator");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(request -> toRequestDto(request))
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateDto eventRequestStatusUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedForUserException("Access denied: User is not an initiator");
        }
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new IllegalStateException("Application confirmation is not required");
        }
        List<Request> requestsToStatusUpdate = requestRepository.findAllByIdIn(eventRequestStatusUpdateDto.getRequestIds());
        if (requestsToStatusUpdate.stream()
                .anyMatch(request -> request.getStatus() != RequestStatus.PENDING.name())) {
            throw new DataConflictException("Applications must be in status \"PENDING\"");
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        RequestStatus updateStatus = eventRequestStatusUpdateDto.getStatus();
        if (updateStatus == RequestStatus.REJECTED || updateStatus == RequestStatus.CONFIRMED) {
            for (Request request : requestsToStatusUpdate)
            {
                if (updateStatus == RequestStatus.REJECTED) {
                    request.setStatus(RequestStatus.REJECTED.name());
                    rejectedRequests.add(request);
                } else if (updateStatus == RequestStatus.CONFIRMED) {
                    request.setStatus(RequestStatus.CONFIRMED.name());
                    confirmedRequests.add(request);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                }

            }
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING.name());
                for (Request pendingRequest : pendingRequests) {
                    pendingRequest.setStatus(RequestStatus.REJECTED.name());
                    rejectedRequests.add(pendingRequest);
                }
                throw new DataConflictException("Application limit exceeded - confirmation not allowed");
            }
        }
        requestRepository.saveAll(confirmedRequests);
        requestRepository.saveAll(rejectedRequests);
        eventRepository.save(event);

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult(
                confirmedRequests.stream()
                        .map(req -> toRequestDto(req))
                        .toList(),
                rejectedRequests.stream()
                        .map(req -> toRequestDto(req))
                        .toList());

        return eventRequestStatusUpdateResult;
    }

    @Override
    public Collection<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        if (rangeStart == null || rangeEnd == null) {
            throw new UnexpectedNullException("RangeStart and rangeEnd must not be null");
        }

        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("RangeStart is not earlier than rangeEnd");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();
        applyUserFilter(predicates, cb, root, users);
        applyStateFilter(predicates, cb, root, states);
        applyCategoryFilter(predicates, cb, root, categories);
        applyDateRangeFilter(predicates, cb, root, rangeStart, rangeEnd);

        if (!predicates.isEmpty()) {
            criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        List<Event> events = typedQuery.getResultList();

        return events.stream()
                .map(event -> toEventDto(event))
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
                throw new DataConflictException("The event date must exceed the current timestamp + 1H");
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
        return toEventDto(eventRepository.save(event));
    }

    // TO DO
    @Override
    public Collection<EventDto> getEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Boolean onlyAvailable, Sort sort, Integer from, Integer size, HttpServletRequest httpServletRequest) {


        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("RangeStart is not earlier than rangeEnd");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        applyTextFilter(predicates, cb, root, text);
        applyCategoryFilter(predicates, cb, root, categories);
        applyPaidFilter(predicates, cb, root, paid);
        applyDateRangeFilter(predicates, cb, root, rangeStart, rangeEnd);
        applyStateFilter(predicates, cb, root, List.of(StateEvent.PUBLISHED.name()));

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

        statsClient.saveHit("main-service", httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr());

        if (onlyAvailable) {
            return events.stream()
                    .filter(event -> event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit())
                    .map(event -> toEventDto(event))
                    .toList();
        }
        return events.stream()
                .map(event -> toEventDto(event))
                .toList();
    }

    // TO DO
    @Override
    public EventDto getEvent(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (!event.getState().equals(StateEvent.PUBLISHED)) {
            throw new DataConflictException("Event must be published");
        }
        statsClient.saveHit("main-service", httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr());

        LocalDateTime start = event.getPublishedOn() == null ? event.getCreatedOn() : event.getPublishedOn();

        List<StatsDto> statistics = statsClient.getStats(start, LocalDateTime.now(),
                List.of(httpServletRequest.getRequestURI()), true).stream()
                .map(obj -> (StatsDto)obj)
                .toList();

        if (statistics.isEmpty()) {
            event.setViews(0L);
        } else {
            event.setViews(statistics.get(0).getHits());
        }

        eventRepository.save(event);
        return toEventDto(event);
    }

    private Location getEventLocation(LocationDto locationDto) {
        Optional<Location> location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        if(location.isEmpty()) {
            return locationRepository.save(toLocation(locationDto, 0L));
        }
        return location.get();
    }

    private void applyDateRangeFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        } else {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
    }

    private void applyCategoryFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, List<Long> categories) {
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
    }

    private void applyStateFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, List<String> states) {
        if (states != null && !states.isEmpty()) {
            predicates.add(cb.in(root.get("state")).value(states));
        }
    }

    private void applyUserFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, List<Long> users) {
        if (users != null && !users.isEmpty()) {
            predicates.add(cb.in(root.get("initiator").get("id")).value(users));
        }
    }

    private void applyTextFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Event> root, String text) {
        if (text != null && !text.isBlank()) {
            String searchText = "%"  + text.toLowerCase() + "%" ;
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
}
