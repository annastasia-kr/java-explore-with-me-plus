package ru.practicum.exception;

public class AccessDeniedForUserException extends RuntimeException {
    public AccessDeniedForUserException(String message) {
        super(message);
    }
}
