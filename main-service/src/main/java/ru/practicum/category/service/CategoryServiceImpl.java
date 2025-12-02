package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.mapper.CategoryMapper.toCategoryDto;
import static ru.practicum.category.mapper.CategoryMapper.toCategory;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return repository.findAll(page).stream()
                .map(obj-> toCategoryDto(obj))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long catId) {
        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена", catId);
                    return new NotFoundException(String.format("Категория с id=%d не найдена", catId));
                });
        return toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto o) {
        Category newCategory = toCategory(o);

        if (repository.existsByName(newCategory.getName())) {
            log.error("Категория с name={} уже существует", newCategory.getName());
            throw new DataConflictException(
                    String.format("Категория с name=%s уже существует", newCategory.getName()));
        }

        return toCategoryDto(repository.save(newCategory));
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
            throw new DataConflictException(String.format("Категория с id=%s связана с событиями", catId));
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

        if (repository.existsByNameAndIdNot(o.getName(), catId)) {
            log.error("Категория с name={} уже существует", o.getName());
            throw new DataConflictException(String.format("Категория с name=%s уже существует", o.getName()));
        }

        existedCategory.setName(o.getName());
        return toCategoryDto(existedCategory);
    }
}