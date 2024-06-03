package ru.daniil.worker.exceptions;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final String message;

    public BaseException(String message) {
        super(message);
        this.message = message;
    }
}
