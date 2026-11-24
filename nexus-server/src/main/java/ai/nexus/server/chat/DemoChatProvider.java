package ai.nexus.server.chat;

import org.springframework.stereotype.Component;

@Component
public class DemoChatProvider implements ChatProvider {

    @Override
    public ChatResponse complete(String message) {
        String answer = "Демо-ответ Nexus AI: я получил сообщение «" + message.trim()
                + "». Следующим этапом можно подключить реального провайдера.";
        return new ChatResponse(answer, "demo", "nexus-demo-1");
    }
}
