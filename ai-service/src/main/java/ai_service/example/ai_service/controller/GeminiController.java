package ai_service.example.ai_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai_service.example.ai_service.service.AI.GeminiService;
import lombok.RequiredArgsConstructor;

@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/gemini")
@RequiredArgsConstructor

public class GeminiController {
    private final GeminiService geminiService;

    @PostMapping("/ask")
    public String askGeminiAPI(@RequestBody String prompt) {
        return geminiService.askGemini(prompt);
    }
    @GetMapping("/hello")
    public String getGeminiInfo() {
        return "Hello From Gemini AI Service";
    }

}
