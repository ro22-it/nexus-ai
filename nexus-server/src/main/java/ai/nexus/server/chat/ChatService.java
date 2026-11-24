package ai.nexus.server.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatProvider provider;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatProvider provider, ChatMessageRepository messageRepository) {
        this.provider = provider;
        this.messageRepository = messageRepository;
    }

    public ChatResponse complete(ChatRequest request, Long userId) {
        String message = request.message();
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Сообщение не должно быть пустым");
        }
        String conversationId = request.sessionId() == null || request.sessionId().isBlank()
                ? "default"
                : request.sessionId().trim();
        ChatResponse response = provider.complete(message);
        messageRepository.save(new ChatMessage(conversationId, userId, "user", message.trim(), response.provider(), response.model()));
        messageRepository.save(new ChatMessage(conversationId, userId, "assistant", response.message(), response.provider(), response.model()));
        return response;
    }

    public java.util.List<ChatMessage> history() {
        return messageRepository.findAllByOrderByCreatedAtAsc();
    }

    public java.util.List<ChatMessage> history(String conversationId, Long userId) {
        return messageRepository.findHistory(conversationId, userId);
    }

    public java.util.List<String> sessions(Long userId) {
        return messageRepository.findConversationIds(userId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteConversation(String conversationId, Long userId) {
        messageRepository.deleteConversation(conversationId, userId);
    }
}
