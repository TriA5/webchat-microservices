package ai_service.example.ai_service.controller; 

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ai_service.example.ai_service.service.AI.GeminiService;
import ai_service.example.ai_service.service.AI.GeminiToxicService; 

@RestController 
// @CrossOrigin(origins = "http://localhost:3000") 
@RequestMapping("/gemini") 
public class GeminiController { 
    private final GeminiService geminiService; 

    private final GeminiToxicService toxicService; 
    
    public GeminiController(GeminiService geminiService, GeminiToxicService toxicService) {
        this.geminiService = geminiService;
        this.toxicService = toxicService;
    }
    
    @PostMapping("/ask") public String askGeminiAPI(@RequestBody String prompt) { 
        return geminiService.askGemini(prompt); 
    } 
    @GetMapping("/hello") 
    public String getGeminiInfo() { 
        return "Hello From Gemini AI Service";
    } 
    
    @PostMapping("/check-toxic") 
    public Map<String, Object> checkToxic(@RequestBody Map<String, String> body) { 
        String text = body.get("text"); 
        boolean toxic = toxicService.isToxic(text);
        Map<String, Object> result = new HashMap<>();
        result.put("toxic", toxic);
        if (toxic) result.put("message", "Nội dung chứa ngôn từ không phù hợp.");
        return result;
    }
    @PostMapping("/check-image-toxic-base64")
    public ResponseEntity<?> checkImageBase64(@RequestBody Map<String, String> body) {
        String base64 = body.get("imageBase64"); // Base64 từ client
        String mimeType = body.get("mimeType"); // optional: "image/jpeg"

        boolean toxic = toxicService.isImageToxicBase64(base64, mimeType);
        return ResponseEntity.ok(Map.of("toxic", toxic));
    }

}