package chat_service.example.chat_service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin()
@RequestMapping("/chats")
public class ChatController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Chat Service!";
    }
}
