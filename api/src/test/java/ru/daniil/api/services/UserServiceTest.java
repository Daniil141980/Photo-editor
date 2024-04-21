package ru.daniil.api.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.daniil.api.domains.UserEntity;
import ru.daniil.api.exceptions.AlreadyExistException;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.repositories.users.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Test
    @DisplayName("Test loadUserByUsername success")
    void loadUserByUsername() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.getByUsername("user")).thenReturn(Optional.of(new UserEntity(1L, "user", "213123")));

        var userService = new UserService(userRepository);
        var result = userService.loadUserByUsername("user");

        verify(userRepository, times(1)).getByUsername("user");
        assertThat(result.username()).isEqualTo("user");
    }

    @Test
    @DisplayName("Test loadUserByUsername failed")
    void loadUserByUsernameFailed() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.getByUsername("user")).thenReturn(Optional.empty());

        var userService = new UserService(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("user"));
        verify(userRepository, times(1)).getByUsername("user");
    }

    @Test
    @DisplayName("Test loadUserById success")
    void loadUserById() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.getById(1L)).thenReturn(Optional.of(new UserEntity(1L, "user", "213123")));

        var userService = new UserService(userRepository);
        var result = userService.loadUserById(1L);

        verify(userRepository, times(1)).getById(1L);
        assertThat(result.username()).isEqualTo("user");
    }

    @Test
    @DisplayName("Test loadUserById failed")
    void loadUserByIdFailed() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.getById(1L)).thenReturn(Optional.empty());

        var userService = new UserService(userRepository);

        assertThrows(NotFoundException.class, () -> userService.loadUserById(1L));
        verify(userRepository, times(1)).getById(1L);
    }

    @Test
    @DisplayName("Test save success")
    void save() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.save("user", "12345")).thenReturn(new UserEntity(1L, "user", "password"));

        var userService = new UserService(userRepository);
        var result = userService.save("user", "12345");

        verify(userRepository, times(1)).existsByUsername("user");
        verify(userRepository, times(1)).save("user", "12345");
        assertThat(result.username()).isEqualTo("user");
    }

    @Test
    @DisplayName("Test save failed")
    void saveFailed() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.existsByUsername("user")).thenReturn(true);
        when(userRepository.save("user", "12345")).thenReturn(new UserEntity(1L, "user", "password"));

        var userService = new UserService(userRepository);

        assertThrows(AlreadyExistException.class, () -> userService.save("user", "12345"));
        verify(userRepository, times(1)).existsByUsername("user");
        verify(userRepository, times(0)).save("user", "12345");
    }
}
