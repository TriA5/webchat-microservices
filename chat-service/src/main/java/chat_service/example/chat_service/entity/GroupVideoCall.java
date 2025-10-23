package chat_service.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "group_video_calls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVideoCall {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupConversation group;

    @Column(name = "initiator_id", nullable = false, length = 36)
    private UUID initiatorId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "group_video_call_participants", joinColumns = @JoinColumn(name = "call_id"))
    @Column(name = "user_id", length = 36)
    private Set<UUID> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CallStatus status = CallStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    public enum CallStatus {
        ACTIVE,
        ENDED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Helper methods
    public void addParticipant(UUID userId) {
        this.participants.add(userId);
    }

    public void removeParticipant(UUID userId) {
        this.participants.remove(userId);
    }

    public boolean hasParticipant(UUID userId) {
        return this.participants.contains(userId);
    }

    public int getParticipantCount() {
        return this.participants.size();
    }

    public void endCall() {
        this.status = CallStatus.ENDED;
        this.endedAt = LocalDateTime.now();
        if (this.createdAt != null && this.endedAt != null) {
            this.durationSeconds = java.time.Duration.between(this.createdAt, this.endedAt).getSeconds();
        }
    }
}

