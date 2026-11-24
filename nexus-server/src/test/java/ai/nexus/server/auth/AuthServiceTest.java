package ai.nexus.server.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void changesPasswordAndRevokesOldSession() {
        AuthResponse registration = authService.register(
                new AuthRequest("password-test-user", "oldpass123"));

        authService.changePassword("Bearer " + registration.token(),
                new ChangePasswordRequest("oldpass123", "newpass123"));

        assertThatThrownBy(() -> authService.profile("Bearer " + registration.token()))
                .hasMessageContaining("Недействительный токен");
        assertThat(authService.login(new AuthRequest("password-test-user", "newpass123")))
                .isNotNull();
    }
}
