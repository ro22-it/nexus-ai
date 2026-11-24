package ai.nexus.server.chat;

import ai.nexus.server.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @PostMapping
    public ChatResponse complete(@RequestBody ChatRequest request,
                                 @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorization) {
        return chatService.complete(request, authService.findUserId(authorization).orElse(null));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidRequest(IllegalArgumentException exception) {
        return exception.getMessage();
    }
}
