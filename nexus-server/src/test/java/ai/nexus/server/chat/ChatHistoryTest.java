package ai.nexus.server.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatHistoryTest {

    @Autowired
    private ChatService chatService;

    @Test
    void savesUserAndAssistantMessages() {
        chatService.complete(new ChatRequest("Сохрани это сообщение", "history-test"), null);

        assertThat(chatService.history("history-test", null))
                .extracting(ChatMessage::getRole)
                .contains("user", "assistant");
    }
}
