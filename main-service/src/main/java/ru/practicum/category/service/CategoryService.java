package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto findById(Long catId);

    CategoryDto create(NewCategoryDto o);

    void deleteById(Long catId);

    CategoryDto updateById(Long catId, NewCategoryDto o);
}