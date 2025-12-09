package ru.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comments.enums.StateComment;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDtoAdmin {

    private StateComment state;
}
