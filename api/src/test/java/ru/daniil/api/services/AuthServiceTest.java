package ru.daniil.api.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.daniil.api.domains.UserEntity;
import ru.daniil.api.exceptions.AlreadyExistException;
import ru.daniil.api.exceptions.BadTokenException;
import ru.daniil.api.exceptions.LoginFailException;
import ru.daniil.api.repositories.tokens.TokenRepository;
import ru.daniil.api.security.JwtService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {
    @Test
    @DisplayName("Test login success")
    void login() {
        var authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManager.authenticate(any())).thenReturn(null);

        var userService = mock(UserService.class);
        when(userService.loadUserByUsername(any())).thenReturn(new UserEntity(1L, "user", "password"));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");

        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.save("mock-refresh-token", 1L)).thenReturn("mock-refresh-token");

        var authService = new AuthService(userService, tokenRepository, authenticationManager, jwtService, mock(PasswordEncoder.class));
        var result = authService.login("user", "password");

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).loadUserByUsername("user");
        verify(jwtService, times(1)).generateRefreshToken(any());
        verify(jwtService, times(1)).generateAccessToken(any());
        verify(tokenRepository, times(1)).save("mock-refresh-token", 1L);
        assertThat(result.user().username()).isEqualTo("user");
    }

    @Test
    @DisplayName("Test login failed")
    void loginFailed() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManager.authenticate(any())).thenThrow(UsernameNotFoundException.class);

        var userService = mock(UserService.class);
        when(userService.loadUserByUsername(any())).thenReturn(new UserEntity(1L, "user", "password"));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");

        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.save("mock-refresh-token", 1L)).thenReturn("mock-refresh-token");

        var authService = new AuthService(userService, tokenRepository, authenticationManager, jwtService, mock(PasswordEncoder.class));
        assertThatThrownBy(() -> authService.login("user", "password"))
                .isInstanceOf(LoginFailException.class)
                .hasMessage("Wrong username or password");

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(0)).loadUserByUsername("user");
        verify(jwtService, times(0)).generateRefreshToken(any());
        verify(jwtService, times(0)).generateAccessToken(any());
        verify(tokenRepository, times(0)).save("mock-refresh-token", 1L);
    }

    @Test
    @DisplayName("Test refreshToken success")
    void refreshToken() {
        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.getUserIdByToken("old-refresh-token")).thenReturn(Optional.of(Pair.of(1L, 1L)));
        when(tokenRepository.updateToken(any(), any())).thenReturn("new-refresh-token");

        var userService = mock(UserService.class);
        when(userService.loadUserById(1L)).thenReturn(new UserEntity(1L, "user", "password"));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");


        var authService = new AuthService(userService, tokenRepository, mock(AuthenticationManager.class), jwtService, mock(PasswordEncoder.class));
        var result = authService.refreshToken("old-refresh-token");

        verify(userService, times(1)).loadUserById(1L);
        verify(jwtService, times(1)).generateRefreshToken(any());
        verify(jwtService, times(1)).generateAccessToken(any());
        verify(tokenRepository, times(1)).getUserIdByToken("old-refresh-token");
        verify(tokenRepository, times(1)).updateToken(any(), any());
        assertThat(result.getFirst()).isEqualTo("new-refresh-token");
        assertThat(result.getSecond()).isEqualTo("mock-access-token");
    }

    @Test
    @DisplayName("Test refreshToken failed")
    void refreshTokenFailed() {
        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.getUserIdByToken("old-refresh-token")).thenReturn(Optional.empty());
        when(tokenRepository.updateToken(any(), any())).thenReturn("new-refresh-token");

        var userService = mock(UserService.class);
        when(userService.loadUserById(1L)).thenReturn(new UserEntity(1L, "user", "password"));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");


        var authService = new AuthService(userService, tokenRepository, mock(AuthenticationManager.class), jwtService, mock(PasswordEncoder.class));
        assertThatThrownBy(() -> authService.refreshToken("old-refresh-token"))
                .isInstanceOf(BadTokenException.class)
                .hasMessage("Bad token, refresh token not found");

        verify(userService, times(0)).loadUserById(1L);
        verify(jwtService, times(0)).generateRefreshToken(any());
        verify(jwtService, times(0)).generateAccessToken(any());
        verify(tokenRepository, times(1)).getUserIdByToken("old-refresh-token");
        verify(tokenRepository, times(0)).updateToken(any(), any());
    }

    @Test
    @DisplayName("Test removeToken success")
    void removeToken() {
        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.existsByToken("token")).thenReturn(true);

        var authService = new AuthService(mock(UserService.class), tokenRepository, mock(AuthenticationManager.class), mock(JwtService.class), mock(PasswordEncoder.class));
        authService.removeToken("token");

        verify(tokenRepository, times(1)).existsByToken("token");
        verify(tokenRepository, times(1)).removeByToken(any());
    }

    @Test
    @DisplayName("Test removeToken failed")
    void removeTokenFailed() {
        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.existsByToken("token")).thenReturn(false);

        var authService = new AuthService(mock(UserService.class), tokenRepository, mock(AuthenticationManager.class), mock(JwtService.class), mock(PasswordEncoder.class));
        assertThatThrownBy(() -> authService.removeToken("token"))
                .isInstanceOf(BadTokenException.class)
                .hasMessage("Bad token, refresh token not found");

        verify(tokenRepository, times(1)).existsByToken("token");
        verify(tokenRepository, times(0)).removeByToken(any());
    }

    @Test
    @DisplayName("Test register success")
    void register() {
        var passwordEncoder = NoOpPasswordEncoder.getInstance();

        var userService = mock(UserService.class);
        when(userService.save(any(), any())).thenReturn(new UserEntity(1L, "user", passwordEncoder.encode("password")));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");

        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.save("mock-refresh-token", 1L)).thenReturn("mock-refresh-token");

        var authService = new AuthService(userService, tokenRepository, mock(AuthenticationManager.class), jwtService, passwordEncoder);
        var result = authService.register("user", "password");

        verify(userService, times(1)).save("user", "password");
        verify(jwtService, times(1)).generateRefreshToken(any());
        verify(jwtService, times(1)).generateAccessToken(any());
        verify(tokenRepository, times(1)).save("mock-refresh-token", 1L);
        assertThat(result.user().username()).isEqualTo("user");
    }

    @Test
    @DisplayName("Test register failed")
    void registerFailed() {
        var passwordEncoder = NoOpPasswordEncoder.getInstance();

        var userService = mock(UserService.class);
        when(userService.save("user", "password")).thenThrow(new AlreadyExistException("User:user already exist"));

        var jwtService = mock(JwtService.class);
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");

        var tokenRepository = mock(TokenRepository.class);
        when(tokenRepository.save("mock-refresh-token", 1L)).thenReturn("mock-refresh-token");

        var authService = new AuthService(userService, tokenRepository, mock(AuthenticationManager.class), jwtService, passwordEncoder);

        assertThatThrownBy(() -> authService.register("user", "password"))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("User:user already exist");

        verify(userService, times(1)).save("user", "password");
        verify(jwtService, times(0)).generateRefreshToken(any());
        verify(jwtService, times(0)).generateAccessToken(any());
        verify(tokenRepository, times(0)).save("mock-refresh-token", 1L);
    }
}
