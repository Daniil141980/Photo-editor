package ru.daniil.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.daniil.api.dto.TokenDto;
import ru.daniil.api.dto.auth.AuthRequestDto;
import ru.daniil.api.dto.auth.AuthResponseDto;
import ru.daniil.api.services.AuthService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "Базовый CRUD API для работы с аутентификацией")
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/register", produces = {MediaType.APPLICATION_JSON_VALUE})
    public AuthResponseDto registerUser(@RequestBody @Valid AuthRequestDto request, HttpServletResponse response) {
        var registeredUser = authService.register(request.username().trim(), request.password().trim());
        addCookie(registeredUser.accessToken(), response);
        return new AuthResponseDto(registeredUser.user().username(), registeredUser.refreshToken());
    }

    @PostMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public AuthResponseDto loginUser(@RequestBody @Valid AuthRequestDto request, HttpServletResponse response) {
        var loginUser = authService.login(request.username().trim(), request.password().trim());
        addCookie(loginUser.accessToken(), response);
        return new AuthResponseDto(loginUser.user().username(), loginUser.refreshToken());
    }

    @PostMapping(value = "/logout")
    public void logoutUser(@RequestBody @Valid TokenDto request, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        SecurityContextHolder.clearContext();
        authService.removeToken(request.token());
    }

    @PostMapping(value = "/refresh", produces = {MediaType.APPLICATION_JSON_VALUE})
    public TokenDto refreshToken(@RequestBody @Valid TokenDto request, HttpServletResponse response) {
        var pairRefreshAndAccessToken = authService.refreshToken(request.token());
        addCookie(pairRefreshAndAccessToken.getSecond(), response);
        return new TokenDto(pairRefreshAndAccessToken.getFirst());
    }

    private void addCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setMaxAge(Math.toIntExact(Duration.ofMinutes(5).getSeconds()));
        response.addCookie(cookie);
    }
}
