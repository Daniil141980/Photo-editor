package ru.daniil.photoeditor.exceptions;

import org.springframework.http.HttpStatus;

public class LoginFailException extends BaseException {
    public LoginFailException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
