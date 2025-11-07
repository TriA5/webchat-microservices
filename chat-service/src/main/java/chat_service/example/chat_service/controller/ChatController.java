package chat_service.example.chat_service.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chat_service.example.chat_service.client.UploadFileClient;
import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.dto.ConversationDTO;
import chat_service.example.chat_service.service.chat.ChatService;
import chat_service.example.chat_service.service.chat.GroupChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final GroupChatService groupChatService;
    private final UploadFileClient uploadFileClient;
    //downloadfile
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

    /**
     * Lấy tin nhắn với pagination (mặc định 10 tin nhắn mới nhất)
     * GET /chats/{conversationId}/messages/paginated?page=0&size=10
     * @param conversationId ID của cuộc hội thoại
     * @param page Số trang (0 = trang đầu tiên với 10 tin nhắn mới nhất)
     * @param size Số lượng tin nhắn mỗi trang (mặc định 10)
     * @return Danh sách tin nhắn
     */
    @GetMapping("/{conversationId}/messages/paginated")
    public List<ChatMessageDTO> getMessagesPaginated(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return chatService.getMessagesPaginated(conversationId, page, size);
    }

    /**
     * Lấy tin nhắn cũ hơn một thời điểm cụ thể (dùng khi scroll lên)
     * GET /chats/{conversationId}/messages/before?timestamp=2024-01-01T10:00:00&size=10
     * @param conversationId ID của cuộc hội thoại
     * @param timestamp Thời điểm (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @param size Số lượng tin nhắn (mặc định 10)
     * @return Danh sách tin nhắn cũ hơn timestamp
     */
    @GetMapping("/{conversationId}/messages/before")
    public List<ChatMessageDTO> getMessagesBeforeTimestamp(
            @PathVariable UUID conversationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam(defaultValue = "10") int size) {
        return chatService.getMessagesBeforeTimestamp(conversationId, timestamp, size);
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

    @GetMapping("/download-file")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @RequestParam String fileUrl,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            ByteArrayResource resource = uploadFileClient.downloadFile(fileUrl, authorizationHeader);
            
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            if (filename.contains("?")) {
                filename = filename.substring(0, filename.indexOf('?'));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
