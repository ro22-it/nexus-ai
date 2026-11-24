package ai.nexus.server.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conversationId;
    private Long userId;
    private String role;
    private String content;
    private String provider;
    private String model;
    private Instant createdAt;

    protected ChatMessage() {
    }

    public ChatMessage(String conversationId, Long userId, String role, String content, String provider, String model) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.provider = provider;
        this.model = model;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
