package ru.practicum.compilations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.service.CompilationService;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper mapper;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        return repository.findAll().stream()
                .map(mapper::toCompilationDto)
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
        return mapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto o) {

    }

    @Override
    @Transactional
    public void deleteById(Long compId) {

    }

    @Override
    public CompilationDto updateById(Long compId, UpdateCompilationRequest o) {

    }
}
