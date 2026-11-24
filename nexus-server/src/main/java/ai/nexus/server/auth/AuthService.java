package ai.nexus.server.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, AuthTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        validateCredentials(request);
        String username = request.username().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }
        User user = userRepository.save(new User(username, passwordEncoder.encode(request.password())));
        return createToken(user);
    }

    public AuthResponse login(AuthRequest request) {
        validateCredentials(request);
        String username = request.username().trim().toLowerCase();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        return createToken(user);
    }

    public Optional<Long> findUserId(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Неверный формат авторизации");
        }
        String tokenValue = authorizationHeader.substring("Bearer ".length()).trim();
        if (tokenValue.isEmpty()) {
            throw new IllegalArgumentException("Пустой токен авторизации");
        }
        Optional<Long> userId = tokenRepository.findByToken(tokenValue)
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .map(AuthToken::getUserId);
        if (userId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный токен");
        }
        return userId;
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        String tokenValue = authorizationHeader.substring("Bearer ".length()).trim();
        tokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.revoke();
            tokenRepository.save(token);
        });
    }

    public ProfileResponse profile(String authorizationHeader) {
        Long userId = findUserId(authorizationHeader)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        return new ProfileResponse(user.getId(), user.getUsername());
    }

    public void changePassword(String authorizationHeader, ChangePasswordRequest request) {
        Long userId = findUserId(authorizationHeader)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация"));
        if (request == null || request.currentPassword() == null || request.newPassword() == null
                || request.newPassword().length() < 8) {
            throw new IllegalArgumentException("Новый пароль должен содержать минимум 8 символов");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль указан неверно");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        tokenRepository.findAllByUserId(userId).forEach(token -> {
            token.revoke();
            tokenRepository.save(token);
        });
    }

    private AuthResponse createToken(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenRepository.save(new AuthToken(tokenValue, user.getId(), Instant.now().plus(30, ChronoUnit.DAYS)));
        return new AuthResponse(tokenValue, user.getId(), user.getUsername());
    }

    private void validateCredentials(AuthRequest request) {
        if (request == null || request.username() == null || request.password() == null
                || request.username().isBlank() || request.password().length() < 8) {
            throw new IllegalArgumentException("Логин обязателен, пароль должен содержать минимум 8 символов");
        }
    }
}
