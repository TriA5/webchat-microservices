package chat_service.example.chat_service.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat_service.example.chat_service.entity.Conversation;
import chat_service.example.chat_service.repository.ConversationRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.Map;

@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepository;

    // Ensure conversation exists between two users. Body: {"user1":"<uuid>", "user2":"<uuid>"}
    @PostMapping("/ensure")
    public ResponseEntity<?> ensureConversation(@RequestBody Map<String, String> body) {
        try {
            UUID u1 = UUID.fromString(body.get("user1"));
            UUID u2 = UUID.fromString(body.get("user2"));

            var existing = conversationRepository.findBetween(u1, u2);
            if (existing.isPresent()) {
                return ResponseEntity.ok(existing.get());
            }

            Conversation c = new Conversation();
            c.setParticipant1(u1);
            c.setParticipant2(u2);
            conversationRepository.save(c);
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }
}
