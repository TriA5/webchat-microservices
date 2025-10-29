package chat_service.example.chat_service.service.chat;

import chat_service.example.chat_service.client.UploadClient;
import chat_service.example.chat_service.client.UserClient;
import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.entity.Conversation;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.Message;
import chat_service.example.chat_service.repository.ConversationRepository;
import chat_service.example.chat_service.repository.GroupConversationRepository;
import chat_service.example.chat_service.repository.GroupMemberRepository;
import chat_service.example.chat_service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service để xử lý gửi tin nhắn (text, image, file)
 */
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final GroupConversationRepository groupConversationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UploadClient uploadClient;
    private final UserClient userClient;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi tin nhắn text trong conversation
     */
    public ChatMessageDTO sendTextMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

        Message message = createMessage(conversation, null, senderId, content, "TEXT", null);
        Message saved = messageRepository.save(message);

        ChatMessageDTO dto = buildMessageDTO(saved, conversationId, null);
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return dto;
    }

    /**
     * Gửi tin nhắn image trong conversation
     */
    public ChatMessageDTO sendImageMessage(UUID conversationId, UUID senderId, MultipartFile imageFile) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

            // Upload image via user-service (base64) to reuse existing Cloudinary integration
            String base64 = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
            String dataUri = "data:" + (imageFile.getContentType() == null ? "image/png" : imageFile.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "chat_" + UUID.randomUUID(), "data", dataUri);
            String imageUrl = uploadClient.uploadBase64(body);

            Message message = createMessage(conversation, null, senderId, "", "IMAGE", imageUrl);
            Message saved = messageRepository.save(message);

            ChatMessageDTO dto = buildMessageDTO(saved, conversationId, null);
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send image message: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi tin nhắn file trong conversation (có thể mở rộng sau)
     */
    public ChatMessageDTO sendFileMessage(UUID conversationId, UUID senderId, MultipartFile file) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

            // Upload file via user-service (base64) to reuse existing Cloudinary integration
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + (file.getContentType() == null ? "application/octet-stream" : file.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "file_" + UUID.randomUUID(), "data", dataUri);
            String fileUrl = uploadClient.uploadBase64(body);

            Message message = createMessage(conversation, null, senderId, file.getOriginalFilename(), "FILE", fileUrl);
            Message saved = messageRepository.save(message);

            ChatMessageDTO dto = buildMessageDTO(saved, conversationId, null);
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send file message: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi tin nhắn text trong group
     */
    public ChatMessageDTO sendGroupTextMessage(UUID groupId, UUID senderId, String content) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        // Ensure sender is member of the group (best-effort via groupMemberRepository using senderId)
    groupMemberRepository.findByGroupAndUser(group, senderId)
                .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

        Message message = createMessage(null, group, senderId, content, "TEXT", null);
        Message saved = messageRepository.save(message);

        ChatMessageDTO dto = buildMessageDTO(saved, null, groupId);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
        return dto;
    }

    /**
     * Gửi tin nhắn image trong group
     */
    public ChatMessageDTO sendGroupImageMessage(UUID groupId, UUID senderId, MultipartFile imageFile) {
        try {
            GroupConversation group = groupConversationRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        groupMemberRepository.findByGroupAndUser(group, senderId)
                    .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

            // Upload image via user-service (base64)
            String base64 = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
            String dataUri = "data:" + (imageFile.getContentType() == null ? "image/png" : imageFile.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "group_chat_" + UUID.randomUUID(), "data", dataUri);
            String imageUrl = uploadClient.uploadBase64(body);

            Message message = createMessage(null, group, senderId, "", "IMAGE", imageUrl);
            Message saved = messageRepository.save(message);

            ChatMessageDTO dto = buildMessageDTO(saved, null, groupId);
            messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send group image message: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi tin nhắn file trong group (có thể mở rộng sau)
     */
    public ChatMessageDTO sendGroupFileMessage(UUID groupId, UUID senderId, MultipartFile file) {
        try {
            GroupConversation group = groupConversationRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        groupMemberRepository.findByGroupAndUser(group, senderId)
                    .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

            // Upload file via user-service (base64)
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + (file.getContentType() == null ? "application/octet-stream" : file.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "group_file_" + UUID.randomUUID(), "data", dataUri);
            String fileUrl = uploadClient.uploadBase64(body);

            Message message = createMessage(null, group, senderId, file.getOriginalFilename(), "FILE", fileUrl);
            Message saved = messageRepository.save(message);

            ChatMessageDTO dto = buildMessageDTO(saved, null, groupId);
            messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send group file message: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method để tạo Message entity
     */
    private Message createMessage(Conversation conversation, GroupConversation group,
                                   UUID senderId, String content, String messageType, String fileUrl) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setGroupConversation(group);
        message.setSender(senderId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setImageUrl(fileUrl); // Dùng chung cho cả image và file
        return message;
    }

    private ChatMessageDTO buildMessageDTO(Message message, UUID conversationId, UUID groupId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(conversationId);
        dto.setGroupId(groupId);
        dto.setSenderId(message.getSender());

        // Try to fetch avatar from user-service (best-effort)
        String avatar = null;
        try {
            chat_service.example.chat_service.client.UserDTO u = userClient.getUserById(message.getSender());
            if (u != null) avatar = u.getAvatar();
        } catch (Exception ignored) {}

        dto.setSenderAvatar(avatar);
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setImageUrl(message.getImageUrl());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileSize(message.getFileSize());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}

