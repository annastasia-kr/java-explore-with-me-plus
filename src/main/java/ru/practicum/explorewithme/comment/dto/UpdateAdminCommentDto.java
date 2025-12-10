package ru.practicum.explorewithme.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminCommentDto {

    @NotBlank(message = "state is required")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "incorrect state value")
    private String state;
}