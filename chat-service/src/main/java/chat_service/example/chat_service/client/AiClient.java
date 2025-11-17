package chat_service.example.chat_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// URL trỏ đến service Gemini (ai-service)
@FeignClient(name = "ai-service")
public interface AiClient {
    // Gọi API kiểm tra toxic
    @PostMapping("/gemini/check-toxic")
    Map<String, Object> checkToxic(@RequestBody Map<String, String> body);
}
