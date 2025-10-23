package chat_service.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_conversation")
    private UUID id;

    @Column(name = "participant1_id", nullable = false)
    private UUID participant1;

    @Column(name = "participant2_id", nullable = false)
    private UUID participant2;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

