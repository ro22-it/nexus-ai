package ai.nexus.server.chat;

public interface ChatProvider {

    ChatResponse complete(String message);
}
