package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsDto {

    @NotBlank(message = "App must not be blank")
    private String app;

    @NotBlank(message = "Uri must not be blank")
    private String uri;

    @NotNull(message = "Hits must not be null")
    private Long hits;

}