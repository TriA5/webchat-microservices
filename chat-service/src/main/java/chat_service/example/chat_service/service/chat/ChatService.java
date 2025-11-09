package chat_service.example.chat_service.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.dto.ConversationDTO;
import chat_service.example.chat_service.entity.Conversation;
import chat_service.example.chat_service.entity.Message;
import chat_service.example.chat_service.repository.ConversationRepository;
import chat_service.example.chat_service.repository.MessageRepository;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // private final UploadImageService uploadImageService;
    // private final FileUploadService fileUploadService;
    private final chat_service.example.chat_service.client.UploadClient uploadClient;
    private final chat_service.example.chat_service.client.UserClient userClient;

    public Conversation ensureConversation(UUID userAId, UUID userBId) {
        try {
            return conversationRepository.findBetween(userAId, userBId).orElseGet(() -> {
                Conversation c = new Conversation();
                c.setParticipant1(userAId);
                c.setParticipant2(userBId);
                Conversation saved = conversationRepository.save(c);
                ConversationDTO dto = new ConversationDTO(saved.getId(), userAId, userBId);
                messagingTemplate.convertAndSend("/topic/conversations/" + userAId, dto);
                messagingTemplate.convertAndSend("/topic/conversations/" + userBId, dto);
                return saved;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure conversation: " + e.getMessage(), e);
        }
    }

    public ChatMessageDTO sendMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        Message m = new Message();
        m.setConversation(conversation);
        m.setSender(senderId);
        m.setContent(content);
        m.setMessageType("TEXT");
        Message saved = messageRepository.save(m);

        String avatar = null;
        try {
            chat_service.example.chat_service.client.UserDTO userDto = userClient.getUserById(senderId);
            if (userDto != null) avatar = userDto.getAvatar();
        } catch (Exception ignored) {
            // best-effort: if user-service is unavailable, proceed without avatar
        }

        ChatMessageDTO dto = new ChatMessageDTO(saved.getId(), conversationId, null, senderId, avatar, content, "TEXT", null, null, null, null, saved.getCreatedAt());
        // publish to conversation topic
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
        return dto;
    }

    public List<ChatMessageDTO> getMessages(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        chat_service.example.chat_service.client.UserDTO u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    return new ChatMessageDTO(
                            m.getId(),
                            conversationId,
                            null,
                            m.getSender(),
                            avatar,
                            m.getContent(),
                            m.getMessageType(),
                            m.getImageUrl(),
                            m.getFileUrl(),
                            m.getFileName(),
                            m.getFileSize(),
                            m.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tin nhắn mới nhất với pagination
     * @param conversationId ID của cuộc hội thoại
     * @param page Số trang (0-based)
     * @param size Số lượng tin nhắn mỗi trang (mặc định 10)
     * @return Danh sách tin nhắn (được sắp xếp từ cũ đến mới để hiển thị đúng thứ tự trên UI)
     */
    public List<ChatMessageDTO> getMessagesPaginated(UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        Pageable pageable = PageRequest.of(page, size);
        
        // Lấy tin nhắn theo thứ tự DESC (mới nhất trước)
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable);
        
        // Đảo ngược danh sách để tin nhắn cũ nhất ở đầu (để hiển thị đúng trên UI)
        Collections.reverse(messages);
        
        return messages.stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        chat_service.example.chat_service.client.UserDTO u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    return new ChatMessageDTO(
                            m.getId(),
                            conversationId,
                            null,
                            m.getSender(),
                            avatar,
                            m.getContent(),
                            m.getMessageType(),
                            m.getImageUrl(),
                            m.getFileUrl(),
                            m.getFileName(),
                            m.getFileSize(),
                            m.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tin nhắn cũ hơn một thời điểm cụ thể (dùng khi scroll lên để load thêm)
     * @param conversationId ID của cuộc hội thoại
     * @param before Thời điểm để lấy tin nhắn trước đó
     * @param size Số lượng tin nhắn (mặc định 10)
     * @return Danh sách tin nhắn
     */
    public List<ChatMessageDTO> getMessagesBeforeTimestamp(UUID conversationId, LocalDateTime before, int size) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        Pageable pageable = PageRequest.of(0, size);
        
        // Lấy tin nhắn cũ hơn thời điểm 'before' theo thứ tự DESC
        List<Message> messages = messageRepository.findByConversationBeforeTimestamp(conversation, before, pageable);
        
        // Đảo ngược để tin nhắn cũ nhất ở đầu
        Collections.reverse(messages);
        
        return messages.stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        chat_service.example.chat_service.client.UserDTO u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    return new ChatMessageDTO(
                            m.getId(),
                            conversationId,
                            null,
                            m.getSender(),
                            avatar,
                            m.getContent(),
                            m.getMessageType(),
                            m.getImageUrl(),
                            m.getFileUrl(),
                            m.getFileName(),
                            m.getFileSize(),
                            m.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<ConversationDTO> getConversationsForUser(UUID userId) {
        return conversationRepository.findByParticipant1OrParticipant2(userId, userId)
                .stream()
                .map(c -> new ConversationDTO(c.getId(), c.getParticipant1(), c.getParticipant2()))
                .collect(Collectors.toList());
    }

    public ChatMessageDTO sendImageMessage(UUID conversationId, UUID senderId, MultipartFile imageFile) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));

            // Diagnostic logging
            if (imageFile != null) {
                log.info("sendImageMessage called: conversationId={} senderId={} filename={} size={} contentType={}",
                        conversationId, senderId, imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());
            } else {
                log.warn("sendImageMessage called with null imageFile: conversationId={} senderId={}", conversationId, senderId);
            }
            // User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Upload image via user-service (base64) to reuse existing Cloudinary integration
            String base64 = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
            String dataUri = "data:" + (imageFile.getContentType() == null ? "image/png" : imageFile.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "chat_" + UUID.randomUUID(), "data", dataUri);
            String imageUrl = uploadClient.uploadBase64(body);
            log.info("uploadClient returned imageUrl={}", imageUrl);

            // Create message
            Message m = new Message();
            m.setConversation(conversation);
            m.setSender(senderId);
            m.setContent(""); // Empty content for image messages
            m.setMessageType("IMAGE");
            m.setImageUrl(imageUrl);
            Message saved = messageRepository.save(m);

            // Create DTO
        String avatar = null;
        try {
        chat_service.example.chat_service.client.UserDTO userDto = userClient.getUserById(senderId);
        if (userDto != null) avatar = userDto.getAvatar();
        } catch (Exception ignored) {}

        ChatMessageDTO dto = new ChatMessageDTO(
            saved.getId(),
            conversationId,
            null,
            senderId,
            avatar,
            saved.getContent(),
            saved.getMessageType(),
            saved.getImageUrl(),
            null, // fileUrl
            null, // fileName
            null, // fileSize
            saved.getCreatedAt()
        );

            // Publish to conversation topic
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
            return dto;
        } catch (Exception e) {
            log.error("Failed to send image message for conversationId={} senderId={}: {}", conversationId, senderId, e.toString(), e);
            throw new RuntimeException("Failed to send image message: " + e.getMessage(), e);
        }
    }

    public ChatMessageDTO sendFileMessage(UUID conversationId, UUID senderId, MultipartFile file) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation không tồn tại"));
            // User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Upload file via user-service (base64) to reuse existing Cloudinary integration
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + (file.getContentType() == null ? "application/octet-stream" : file.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "file_" + UUID.randomUUID(), "data", dataUri);
            String fileUrl = uploadClient.uploadFile(file, "file_" + UUID.randomUUID());

            // Create message
            Message m = new Message();
            m.setConversation(conversation);
            m.setSender(senderId);
            m.setContent(""); // Empty content for file messages
            m.setMessageType("FILE");
            m.setFileUrl(fileUrl);
            m.setFileName(file.getOriginalFilename());
            m.setFileSize(file.getSize());
            Message saved = messageRepository.save(m);

            // Create DTO
        String avatar = null;
        try {
        chat_service.example.chat_service.client.UserDTO userDto = userClient.getUserById(senderId);
        if (userDto != null) avatar = userDto.getAvatar();
        } catch (Exception ignored) {}

        ChatMessageDTO dto = new ChatMessageDTO(
            saved.getId(),
            conversationId,
            null,
            senderId,
            avatar,
            saved.getContent(),
            saved.getMessageType(),
            null, // imageUrl
            saved.getFileUrl(),
            saved.getFileName(),
            saved.getFileSize(),
            saved.getCreatedAt()
        );

            // Publish to conversation topic
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId, dto);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send file message: " + e.getMessage(), e);
        }
    }
    //Delete message
    public void deleteMessage(UUID messageId, UUID userId) {
        try {
            Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message không tồn tại"));
            if (!message.getSender().equals(userId)) {
                throw new RuntimeException("Bạn không có quyền xóa message này");
            }
            String linkUrl = message.getImageUrl() != null ? message.getImageUrl() : message.getFileUrl();
            if(message.getMessageType().equals("IMAGE")) {
                uploadClient.deleteByImageUrl(linkUrl);
            } else if(message.getMessageType().equals("FILE")) {
                uploadClient.deleteByVideoUrl(linkUrl);
            }else{
                log.info("Message type is neither IMAGE nor FILE, no associated file to delete.");
            }
            messageRepository.delete(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Thất bại khi xóa message: " + e.getMessage(), e);
        }
    }
    //Lấy tất cã các ảnh theo id cuộc trò chuyện
    public List<Message> getImageMessagesByConversationId(UUID conversationId) {
        return messageRepository.findImagesByConversation(conversationId);
    }
    //Lấy tất cã các File theo id cuộc trò chuyện
    public List<Message> getFileMessagesByConversationId(UUID conversationId) {
        return messageRepository.findFilesByConversation(conversationId);
    }
}