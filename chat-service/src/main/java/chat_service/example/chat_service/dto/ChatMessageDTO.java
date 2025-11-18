package chat_service.example.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private UUID id;
    private UUID conversationId; // null for group chat
    private UUID groupId;       // null for private chat
    private UUID senderId;
    private String senderAvatar;
    private String content;
    private String messageType; // TEXT, IMAGE, FILE
    private String imageUrl;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createdAt;
    
    // Image validation fields
    private Boolean isSexy;          // true if image contains sexy/porn/hentai content
    private Double sexyScore;        // confidence score for sexy content
    private Double pornScore;        // confidence score for porn content
    private Double hentaiScore;      // confidence score for hentai content
    private String topLabel;         // top label: "sexy", "porn", "hentai", or "neutral"
    private String validationMessage; // message from AI service
    
    // Constructor for backward compatibility
    public ChatMessageDTO(UUID id, UUID conversationId, UUID senderId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = "TEXT";
        this.createdAt = createdAt;
    }
}

