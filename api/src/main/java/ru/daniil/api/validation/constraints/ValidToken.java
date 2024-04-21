package ru.daniil.api.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.daniil.api.validation.validators.TokenValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TokenValidator.class)
@Documented
public @interface ValidToken {
    String message() default "Invalid token";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}