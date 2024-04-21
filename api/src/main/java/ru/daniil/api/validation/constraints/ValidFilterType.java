package ru.daniil.api.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.daniil.api.validation.validators.FilterTypeValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FilterTypeValidator.class)
@Documented
public @interface ValidFilterType {
    String message() default "Unacceptable filter type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
