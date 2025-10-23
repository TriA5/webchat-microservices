package chat_service.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "video_call")
public class VideoCall {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_video_call")
    private UUID id;

    @Column(name = "caller_id", nullable = false)
    private UUID caller;

    @Column(name = "callee_id", nullable = false)
    private UUID callee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CallStatus status = CallStatus.INITIATED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    public enum CallStatus {
        INITIATED,    // Call started by caller
        RINGING,      // Callee is being notified
        ACCEPTED,     // Call accepted by callee
        REJECTED,     // Call rejected by callee
        ENDED,        // Call ended by either party
        TIMEOUT       // Call timed out without answer
    }
}
