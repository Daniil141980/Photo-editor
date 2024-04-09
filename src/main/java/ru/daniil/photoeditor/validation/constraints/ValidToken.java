package ru.daniil.photoeditor.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.daniil.photoeditor.validation.validators.TokenValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TokenValidator.class)
@Documented
public @interface ValidToken {
    String message() default "Invalid token";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}