package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HitDto {

    private Long id;

    @NotNull(message = "App must not be null")
    @NotBlank(message = "App must not be blank")
    private String app;

    @NotNull(message = "Uri must not be null")
    @NotBlank(message = "Uri must not be blank")
    private String uri;
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
