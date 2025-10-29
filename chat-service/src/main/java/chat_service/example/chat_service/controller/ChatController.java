package chat_service.example.chat_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.dto.ConversationDTO;
import chat_service.example.chat_service.service.chat.ChatService;
import chat_service.example.chat_service.service.chat.GroupChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;

    private final GroupChatService groupChatService; // Đã thêm inject GroupChatService

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Chat Service!";
    }
    @GetMapping("/hello-everyone")
    public String helloEveryone() {
        return "Hello everyone from Chat Service!";
    }
    @PostMapping("/ensure")
    public UUID ensureConversation(@RequestParam UUID userAId, @RequestParam UUID userBId) {
        return chatService.ensureConversation(userAId, userBId).getId();
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageDTO> getMessages(@PathVariable UUID conversationId) {
        return chatService.getMessages(conversationId);
    }

    @GetMapping("/conversations")
    public List<ConversationDTO> listConversations(@RequestParam UUID userId) {
        return chatService.getConversationsForUser(userId);
    }

    @Data
    public static class SendMessagePayload {
        private UUID conversationId;
        private UUID senderId;
        private String content;
    }

    @MessageMapping("/chat.send")
    public void onSendMessage(@Payload ChatMessageDTO payload) { // Đồng bộ với ChatMessageDTO
        chatService.sendMessage(payload.getConversationId(), payload.getSenderId(), payload.getContent());
    }

    @MessageMapping("/group.send")
    public void onSendGroupMessage(@Payload ChatMessageDTO payload) {
        groupChatService.sendGroupMessage(payload.getGroupId(), payload.getSenderId(), payload.getContent());
    }

    @PostMapping("/{conversationId}/send-image")
    public ChatMessageDTO sendImageMessage(
            @PathVariable UUID conversationId,
            @RequestParam UUID senderId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile imageFile) {
        return chatService.sendImageMessage(conversationId, senderId, imageFile);
    }

    @PostMapping("/group/{groupId}/send-image")
    public ChatMessageDTO sendGroupImageMessage(
            @PathVariable UUID groupId,
            @RequestParam UUID senderId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile imageFile) {
        return groupChatService.sendGroupImageMessage(groupId, senderId, imageFile);
    }

    @PostMapping("/{conversationId}/send-file")
    public ChatMessageDTO sendFileMessage(
            @PathVariable UUID conversationId,
            @RequestParam UUID senderId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return chatService.sendFileMessage(conversationId, senderId, file);
    }

    @PostMapping("/group/{groupId}/send-file")
    public ChatMessageDTO sendGroupFileMessage(
            @PathVariable UUID groupId,
            @RequestParam UUID senderId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return groupChatService.sendGroupFileMessage(groupId, senderId, file);
    }
}
