package ai.nexus.server.api;

import ai.nexus.server.chat.OpenAiChatProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProviderController {

    private final OpenAiChatProvider openAiProvider;
    private final String configuredProvider;
    private final String openAiModel;

    public ProviderController(OpenAiChatProvider openAiProvider,
                              @Value("${nexus.ai.provider:demo}") String configuredProvider,
                              @Value("${openai.model:gpt-6.2-astra}") String openAiModel) {
        this.openAiProvider = openAiProvider;
        this.configuredProvider = configuredProvider;
        this.openAiModel = openAiModel;
    }

    @GetMapping("/api/providers")
    public List<ProviderInfo> providers() {
        boolean openAiActive = "openai".equalsIgnoreCase(configuredProvider) && openAiProvider.isConfigured();
        return List.of(
                new ProviderInfo("demo", "Nexus Demo", true, !openAiActive, "nexus-demo-1"),
                new ProviderInfo("openai", "OpenAI", openAiProvider.isConfigured(), openAiActive, openAiModel)
        );
    }
}
