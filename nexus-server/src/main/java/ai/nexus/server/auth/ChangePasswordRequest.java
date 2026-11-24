package ai.nexus.server.auth;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}
