package ru.daniil.api.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.daniil.api.domains.FilterType;
import ru.daniil.api.validation.constraints.ValidFilterType;

import java.util.List;

public class FilterTypeValidator implements ConstraintValidator<ValidFilterType, List<String>> {
    @Override
    public void initialize(ValidFilterType constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<String> filters, ConstraintValidatorContext context) {
        try {
            filters.forEach(FilterType::valueOf);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}