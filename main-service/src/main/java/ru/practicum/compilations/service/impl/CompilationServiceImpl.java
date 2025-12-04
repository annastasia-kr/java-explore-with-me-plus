package ru.practicum.compilations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.service.CompilationService;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final EventRepository eventRepository;
    private final CompilationMapper mapper;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return repository.findByPinned(pinned, page).getContent().stream()
                .map(compilation -> {
                    CompilationDto dto = mapper.toCompilationDto(compilation);
                    if (compilation.getEvents() != null) {
                        dto.setEvents(mapper.toEventShortDtoCollection(compilation.getEvents()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CompilationDto> findAll(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return repository.findAll(page).getContent().stream()
                .map(compilation -> {
                    CompilationDto dto = mapper.toCompilationDto(compilation);
                    if (compilation.getEvents() != null) {
                        dto.setEvents(mapper.toEventShortDtoCollection(compilation.getEvents()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompilationDto findById(Long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Подборка с id={} не найдена", compId);
                    return new NotFoundException(String.format("Подборка с id=%d не найдена", compId));
                });
        CompilationDto dto = mapper.toCompilationDto(compilation);
        if (compilation.getEvents() != null) {
            dto.setEvents(mapper.toEventShortDtoCollection(compilation.getEvents()));
        }
        return dto;
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto o) {
        Compilation newCompilation = mapper.toCompilation(o);

        if (o.getEvents() != null && !o.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(o.getEvents());

            if (events.size() != o.getEvents().size()) {
                Set<Long> foundEventIds = events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toSet());

                List<Long> notFoundEventIds = o.getEvents().stream()
                        .filter(id -> !foundEventIds.contains(id))
                        .collect(Collectors.toList());

                log.error("События с id={} не найдены", notFoundEventIds);
                throw new NotFoundException(String.format("События с id=%s не найдены", notFoundEventIds));
            }

            newCompilation.setEvents(events);
        }

        CompilationDto dto = mapper.toCompilationDto(repository.save(newCompilation));
        if (newCompilation.getEvents() != null) {
            dto.setEvents(mapper.toEventShortDtoCollection(newCompilation.getEvents()));
        } else {
            dto.setEvents(Collections.emptyList());
        }
        return dto;
    }

    @Override
    @Transactional
    public void deleteById(Long compId) {
        if (!repository.existsById(compId)) {
            log.error("Подборка с id={} не найдена", compId);
            throw new NotFoundException(String.format("Подборка с id=%s не найдена", compId));
        }
        repository.deleteById(compId);
    }

    @Override
    public CompilationDto updateById(Long compId, UpdateCompilationRequest o) {
        Compilation existedCompilation = repository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Подборка с id={} не найдена", compId);
                    return new NotFoundException(String.format("Подборка с id=%s не найдена", compId));
                });
        if (o.getPinned() != null) {
            existedCompilation.setPinned(o.getPinned());
        }
        if (o.getTitle() != null && !o.getTitle().isBlank()) {
            existedCompilation.setTitle(o.getTitle());
        }
        if (o.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(o.getEvents());
            existedCompilation.setEvents(events);
        }
        CompilationDto dto = mapper.toCompilationDto(existedCompilation);
        if (existedCompilation.getEvents() != null) {
            dto.setEvents(mapper.toEventShortDtoCollection(existedCompilation.getEvents()));
        }
        return dto;
    }
}
