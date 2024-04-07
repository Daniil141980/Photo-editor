package ru.daniil.photoeditor.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyExistException extends BaseException {

    public AlreadyExistException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
