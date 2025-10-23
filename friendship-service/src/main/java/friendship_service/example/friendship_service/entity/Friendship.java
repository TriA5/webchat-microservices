package friendship_service.example.friendship_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "friendship")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_friendship")
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;   // ID người gửi lời mời

    @Column(name = "addressee_id", nullable = false)
    private UUID addresseeId;   // ID người nhận lời mời

    @Column(name = "status", nullable = false)
    private String status;    // PENDING | ACCEPTED | REJECTED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

