package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.events.model.enumeration.StateActionAdmin;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventDtoAdminRequest {

    private String annotation;

    private String title;

    private Long category;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Boolean paid;

    private Long participantLimit;

    private Boolean requestModeration;

    private LocationDto location;

    private StateActionAdmin stateAction;

}
