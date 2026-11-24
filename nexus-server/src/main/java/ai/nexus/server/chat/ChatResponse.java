package ai.nexus.server.chat;

public record ChatResponse(String message, String provider, String model) {
}
