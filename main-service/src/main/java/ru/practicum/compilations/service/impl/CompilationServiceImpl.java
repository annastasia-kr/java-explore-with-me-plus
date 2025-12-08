package ru.practicum.compilations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
        Page<Compilation> compilations;

        if (pinned != null) {
            compilations = repository.findByPinned(pinned, page);
        } else {
            compilations = repository.findAll(page);
        }
        return compilations.getContent().stream()
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
    public CompilationDto create(NewCompilationDto newCompilationData) {
        Compilation newCompilation = mapper.toCompilation(newCompilationData);

        if (newCompilationData.getEvents() != null && !newCompilationData.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(newCompilationData.getEvents());

            if (events.size() != newCompilationData.getEvents().size()) {
                Set<Long> foundEventIds = events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toSet());

                List<Long> notFoundEventIds = newCompilationData.getEvents().stream()
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
    @Transactional
    public CompilationDto updateById(Long compId, UpdateCompilationRequest compilationData) {
        Compilation existedCompilation = repository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Подборка с id={} не найдена", compId);
                    return new NotFoundException(String.format("Подборка с id=%s не найдена", compId));
                });
        if (compilationData.getPinned() != null) {
            existedCompilation.setPinned(compilationData.getPinned());
        }
        if (compilationData.getTitle() != null && !compilationData.getTitle().isBlank()) {
            existedCompilation.setTitle(compilationData.getTitle());
        }
        if (compilationData.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationData.getEvents());
            existedCompilation.setEvents(events);
        }
        Compilation savedCompilation = repository.save(existedCompilation);
        CompilationDto dto = mapper.toCompilationDto(savedCompilation);
        if (savedCompilation.getEvents() != null) {
            dto.setEvents(mapper.toEventShortDtoCollection(savedCompilation.getEvents()));
        }
        return dto;
    }
}
