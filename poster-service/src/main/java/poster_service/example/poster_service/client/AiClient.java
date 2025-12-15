package poster_service.example.poster_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// URL trỏ đến service Gemini (ai-service)
@FeignClient(name = "ai-service",contextId = "AiClient")
public interface AiClient {
    // Gọi API kiểm tra ảnh sexy/porn/hentai (Base64)
    @PostMapping("/huggingface/is-image-sexy-base64")
    Map<String, Object> checkImageSexyBase64(@RequestBody Map<String, Object> body);

    // Gọi API kiểm tra toxic
    @PostMapping("/gemini/check-toxic")
    Map<String, Object> checkToxic(@RequestBody Map<String, String> body);
}
