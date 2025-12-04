package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Annotation must not be blank")
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 3, max = 120)
    private String title;

    @NotNull
    private Long category;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Boolean paid = false;

    @PositiveOrZero
    private Long participantLimit = 0L;

    private Boolean requestModeration = true;

    @NotNull
    private LocationDto location;
}
