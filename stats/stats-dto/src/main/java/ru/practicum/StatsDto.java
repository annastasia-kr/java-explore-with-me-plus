package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
public class StatsDto {

    @NotBlank(message = "App must not be blank")
    private String app;

    @NotBlank(message = "Uri must not be blank")
    private String uri;

    @NotNull(message = "Hits must not be null")
    @Positive
    private Long hits;

}