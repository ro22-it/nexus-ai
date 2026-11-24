package ai.nexus.server.chat;

import ai.nexus.server.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatSessionController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @GetMapping
    public List<String> sessions(@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization) {
        return chatService.sessions(authService.findUserId(authorization).orElse(null));
    }
}
