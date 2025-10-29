package chat_service.example.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallDTO {
    private UUID id;
    private UUID callerId;
    private String callerName;
    private String callerAvatar;
    private UUID calleeId;
    private String calleeName;
    private String calleeAvatar;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
}
