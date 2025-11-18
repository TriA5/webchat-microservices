package ai_service.example.ai_service.service.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class GeminiToxicService {

    @Value("${google.genai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isToxic(String text) {
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + apiKey;

        String prompt = """
                Bạn là hệ thống lọc nội dung.
                Hãy đọc văn bản sau và trả lời bằng JSON đơn giản dạng {"toxic": true/false}.
                Văn bản: "%s"
                """.formatted(text);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(geminiUrl, HttpMethod.POST, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List parts = (List) content.get("parts");
                String output = (String) ((Map) parts.get(0)).get("text");

                return output.toLowerCase().contains("\"toxic\": true");
            }
        } catch (Exception e) {
            System.err.println("Lỗi gọi Gemini API: " + e.getMessage());
        }

        return false;
    }

    public boolean isImageToxicBase64(String base64Image, String mimeType) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + apiKey;

        try {
            String prompt = """
                    Bạn là hệ thống lọc nội dung hình ảnh.
                    Hãy phân tích ảnh và trả về JSON dạng {"toxic": true/false}.
                    Nếu ảnh chứa NSFW, bạo lực, xúc phạm, tình dục, hãy trả về {"toxic": true}.
                    """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt),
                                            Map.of(
                                                    "inline_data", Map.of(
                                                            "mime_type", mimeType != null ? mimeType : "image/jpeg",
                                                            "data", base64Image))))));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            List candidates = (List) res.getBody().get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            String output = (String) ((Map) parts.get(0)).get("text");

            return output.toLowerCase().contains("\"toxic\": true");

        } catch (Exception e) {
            System.err.println("Lỗi khi phân tích ảnh: " + e.getMessage());
        }

        return false;
    }

    // private String encodeToBase64(MultipartFile file) throws Exception {
    // return Base64.getEncoder().encodeToString(file.getBytes());
    // }
}
