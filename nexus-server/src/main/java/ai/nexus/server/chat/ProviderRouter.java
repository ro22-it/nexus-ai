package ai.nexus.server.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ProviderRouter implements ChatProvider {

    private final DemoChatProvider demoProvider;
    private final OpenAiChatProvider openAiProvider;
    private final String configuredProvider;

    public ProviderRouter(
            DemoChatProvider demoProvider,
            OpenAiChatProvider openAiProvider,
            @Value("${nexus.ai.provider:demo}") String configuredProvider) {
        this.demoProvider = demoProvider;
        this.openAiProvider = openAiProvider;
        this.configuredProvider = configuredProvider;
    }

    @Override
    public ChatResponse complete(String message) {
        if ("openai".equalsIgnoreCase(configuredProvider) && openAiProvider.isConfigured()) {
            return openAiProvider.complete(message);
        }
        return demoProvider.complete(message);
    }
}
