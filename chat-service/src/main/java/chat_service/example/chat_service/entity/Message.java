package chat_service.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_message")
    private UUID id;

    // @ManyToOne(optional = false)
    // @JoinColumn(name = "conversation_id", nullable = false)
    @ManyToOne
    @JoinColumn(name = "conversation_id") // Bỏ optional=false và nullable=false
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "group_conversation_id")
    private GroupConversation groupConversation;

    @Column(name = "sender_id", nullable = false)
    private UUID sender;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", nullable = false)
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    @PreUpdate
    private void validate() {
        if (conversation != null && groupConversation != null) {
            throw new IllegalStateException("Tin nhắn không thể thuộc cả Conversation và GroupConversation");
        }
        if (conversation == null && groupConversation == null) {
            throw new IllegalStateException("Tin nhắn phải thuộc Conversation hoặc GroupConversation");
        }
    }
}
