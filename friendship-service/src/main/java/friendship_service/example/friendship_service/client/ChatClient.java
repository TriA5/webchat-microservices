package friendship_service.example.friendship_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "chat-service")
public interface ChatClient {
    @PostMapping("/conversations/ensure")
    ResponseEntity<?> ensureConversation(@RequestBody Map<String, String> body);
}
