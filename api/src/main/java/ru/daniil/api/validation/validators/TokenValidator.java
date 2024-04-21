package ru.daniil.api.validation.validators;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import ru.daniil.api.validation.constraints.ValidToken;

public class TokenValidator implements ConstraintValidator<ValidToken, String> {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void initialize(ValidToken constraintAnnotation) {
    }

    @Override
    public boolean isValid(String token, ConstraintValidatorContext context) {
        return (validateToken(token));
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}