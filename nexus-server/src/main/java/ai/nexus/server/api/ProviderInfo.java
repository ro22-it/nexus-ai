package ai.nexus.server.api;

public record ProviderInfo(String id, String name, boolean configured, boolean active, String model) {
}
