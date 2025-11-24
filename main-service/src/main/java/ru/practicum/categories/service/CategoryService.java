package ru.practicum.categories.service;

import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto findById(Long catId);

    CategoryDto create(NewCategoryDto o);

    void deleteById(Long catId);

    CategoryDto updateById(Long catId, NewCategoryDto o);
}
