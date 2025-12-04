package ru.practicum.requests.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.requests.enums.RequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateDto {

    @NotEmpty(message = "List must not be empty")
    @NotNull(message = "List elements cannot be null")
    private List<Long> requestIds;

    @NotNull(message = "Status cannot be null")
    private RequestStatus status;
}
