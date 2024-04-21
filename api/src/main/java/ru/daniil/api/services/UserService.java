package ru.daniil.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.api.domains.UserEntity;
import ru.daniil.api.exceptions.AlreadyExistException;
import ru.daniil.api.exceptions.NotFoundException;
import ru.daniil.api.repositories.users.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User:%s not found".formatted(username))
        );
    }

    @Transactional
    public UserEntity save(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new AlreadyExistException("User:%s already exist".formatted(username));
        }
        return userRepository.save(username, password);
    }

    public UserEntity loadCurrentUser() {
        return loadUserByUsername(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername());
    }

    public UserEntity loadUserById(Long id) {
        return userRepository.getById(id).orElseThrow(
                () -> new NotFoundException("User:%s not found".formatted(id))
        );
    }
}