package ru.daniil.api.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.daniil.api.domains.FilterType;
import ru.daniil.api.validation.constraints.ValidFilterType;

public class FilterTypeValidator implements ConstraintValidator<ValidFilterType, String> {
    @Override
    public void initialize(ValidFilterType constraintAnnotation) {
    }

    @Override
    public boolean isValid(String filter, ConstraintValidatorContext context) {
        try {
            FilterType.valueOf(filter);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}