package ru.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {

    @NotBlank(message = "Text must not be blank")
    @Size(min = 5, max = 500, message = "Text must be between 5 and 500 characters")
    private String text;

    @NotNull
    private Long authorId;
}
