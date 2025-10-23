package chat_service.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "group_member")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_member")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupConversation group;

    @Column(name = "user_id", nullable = false)
    private UUID user;

    @Column(name = "role", nullable = false)
    private String role; // ADMIN or MEMBER

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}

