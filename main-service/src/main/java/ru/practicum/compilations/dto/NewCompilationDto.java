package ru.practicum.compilations.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class NewCompilationDto {
    private Boolean pinned;

    @NotBlank(message = "'title' is required.")
    @Size(min = 1, max = 50)
    private String title;

    private List<Integer> events;
}
