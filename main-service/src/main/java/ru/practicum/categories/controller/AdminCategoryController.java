package ru.practicum.categories.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryDto newCategoryData) {
        return service.create(newCategoryData);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable @Positive Long catId) {
        service.deleteById(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateById(@PathVariable @Positive Long catId,
                                  @RequestBody @Valid NewCategoryDto categoryData) {
        return service.updateById(catId, categoryData);
    }
}
