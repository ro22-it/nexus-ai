package ai.nexus.server.auth;

public record AuthResponse(String token, Long userId, String username) {
}
