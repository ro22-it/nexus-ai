package ai.nexus.server.chat;

import ai.nexus.server.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/chat/history")
public class ChatHistoryController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatHistoryController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @GetMapping
    public List<ChatMessage> history(@RequestParam(defaultValue = "default") String sessionId,
                                     @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization) {
        return chatService.history(sessionId, authService.findUserId(authorization).orElse(null));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(defaultValue = "default") String sessionId,
                       @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization) {
        chatService.deleteConversation(sessionId, authService.findUserId(authorization).orElse(null));
    }
}
