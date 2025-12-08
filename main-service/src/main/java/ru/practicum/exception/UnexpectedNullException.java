package ru.practicum.exception;

public class UnexpectedNullException extends RuntimeException {
    public UnexpectedNullException(String message) {
        super(message);
    }
}
