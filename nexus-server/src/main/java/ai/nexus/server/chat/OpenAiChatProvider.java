package ai.nexus.server.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class OpenAiChatProvider implements ChatProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public OpenAiChatProvider(
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-6.2-astra}") String model,
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    @Override
    public ChatResponse complete(String message) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("model", model)
                    .put("input", message)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/responses"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("OpenAI API вернул HTTP " + response.statusCode());
            }

            JsonNode body = objectMapper.readTree(response.body());
            String answer = body.path("output_text").asText();
            if (answer.isBlank()) {
                throw new IllegalStateException("OpenAI API вернул пустой ответ");
            }
            return new ChatResponse(answer, "openai", model);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось прочитать ответ OpenAI API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос к OpenAI API был прерван", exception);
        }
    }
}
