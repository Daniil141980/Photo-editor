package ru.daniil.photoeditor.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.daniil.photoeditor.validation.validators.FileFormatValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileFormatValidator.class)
@Documented
public @interface FileFormat {
    String value();

    String message() default "Not acceptable file's content type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}