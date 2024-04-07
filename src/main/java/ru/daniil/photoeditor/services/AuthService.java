package ru.daniil.photoeditor.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.daniil.photoeditor.domains.UserEntity;
import ru.daniil.photoeditor.exceptions.BadTokenException;
import ru.daniil.photoeditor.exceptions.LoginFailException;
import ru.daniil.photoeditor.repositories.tokens.TokenRepository;
import ru.daniil.photoeditor.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ServiceResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            var user = userService.loadUserByUsername(username);
            return new ServiceResponse(user,
                    tokenRepository.save(jwtService.generateRefreshToken(user), user.id()),
                    jwtService.generateAccessToken(user));
        } catch (AuthenticationException e) {
            throw new LoginFailException("Wrong username or password");
        }
    }

    @Transactional
    public ServiceResponse register(String username, String password) {
        var registeredUser = userService.save(username, passwordEncoder.encode(password));
        return new ServiceResponse(registeredUser,
                tokenRepository.save(jwtService.generateRefreshToken(registeredUser), registeredUser.id()),
                jwtService.generateAccessToken(registeredUser));
    }

    @Transactional
    public Pair<String, String> refreshToken(String oldToken) {
        var pairIdAndUserId = tokenRepository.getUserIdByToken(oldToken);
        if (pairIdAndUserId.isEmpty()) {
            throw new BadTokenException("Bad token, refresh token not found");
        }
        var user = userService.loadUserById(pairIdAndUserId.get().getSecond());
        return Pair.of(tokenRepository.updateToken(pairIdAndUserId.get().getFirst(),
                jwtService.generateRefreshToken(user)), jwtService.generateAccessToken(user));
    }

    @Transactional
    public void removeToken(String token) {
        if (!tokenRepository.existsByToken(token)) {
            throw new BadTokenException("Bad token, refresh token not found");
        }
        tokenRepository.removeByToken(token);
    }

    public record ServiceResponse(UserEntity user,
                                  String refreshToken,
                                  String accessToken) {
    }
}