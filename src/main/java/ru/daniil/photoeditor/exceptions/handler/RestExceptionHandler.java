package ru.daniil.photoeditor.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.daniil.photoeditor.dto.SuccessResponseDto;
import ru.daniil.photoeditor.exceptions.BaseException;
import ru.daniil.photoeditor.exceptions.StorageException;

@RestControllerAdvice
public class RestExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SuccessResponseDto methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            return new SuccessResponseDto(
                    false,
                    ex.getBody().getTitle() + ": " + ex.getBody().getDetail()
            );
        }
        return new SuccessResponseDto(
                false,
                ex.getBody().getTitle() + ": " + fieldError.getField() + " - " + fieldError.getDefaultMessage()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public SuccessResponseDto handlerMethodValidationException(HandlerMethodValidationException ex) {
        var detailMessageArguments = ex.getDetailMessageArguments();
        if (detailMessageArguments == null || detailMessageArguments.length == 0) {
            return new SuccessResponseDto(
                    false,
                    ex.getMessage()
            );
        }
        return new SuccessResponseDto(
                false,
                ex.getStatusCode() + ": " + detailMessageArguments[0]
        );
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<SuccessResponseDto> baseExceptionHandler(BaseException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new SuccessResponseDto(false, ex.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<SuccessResponseDto> storageExceptionHandler(StorageException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new SuccessResponseDto(false, ex.getMessage()));
    }
}
