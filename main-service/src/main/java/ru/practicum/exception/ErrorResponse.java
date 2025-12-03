package ru.practicum.exception;

public class ErrorResponse {

    private final String status;

    public ErrorResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
