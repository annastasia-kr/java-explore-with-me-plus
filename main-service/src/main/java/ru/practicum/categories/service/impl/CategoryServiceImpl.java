package ru.practicum.categories.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.categories.service.CategoryService;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.DatabaseConstraintException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        return repository.findAll().stream()
                .map(mapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена", catId);
                    return new NotFoundException(String.format("Категория с id=%d не найдена", catId));
                });
        return mapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto o) {
        Category newCategory = mapper.toCategory(o);

        if (repository.existsByName(newCategory.getName())) {
            log.error("Категория с name={} уже существует", newCategory.getName());
            throw new DatabaseConstraintException(String.format("Категория с name=%s уже существует", newCategory.getName()));
        }

        return mapper.toCategoryDto(repository.save(newCategory));
    }

    @Override
    @Transactional
    public void deleteById(Long catId) {
        if (!repository.existsById(catId)) {
            log.error("Категория с id={} не найдена", catId);
            throw new NotFoundException(String.format("Категория с id=%s не найдена", catId));
        }
        if (eventRepository.existsByCategoryId(catId)) {
            log.error("Категория с id={} связана с событиями", catId);
            throw new DatabaseConstraintException(String.format("Категория с id=%s связана с событиями", catId));
        }

        repository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateById(Long catId, NewCategoryDto o) {
        Category existedCategory = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена", catId);
                    return new NotFoundException(String.format("Категория с id=%s не найдена", catId));
                });

        if (repository.existsByName(o.getName())) {
            log.error("Категория с name={} уже существует", o.getName());
            throw new DatabaseConstraintException(String.format("Категория с name=%s уже существует", o.getName()));
        }

        existedCategory.setName(o.getName());
        return mapper.toCategoryDto(existedCategory);
    }
}
