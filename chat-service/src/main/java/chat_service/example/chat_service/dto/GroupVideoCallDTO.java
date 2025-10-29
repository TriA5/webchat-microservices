package chat_service.example.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Group Video Call
 * Used to transfer call data between backend and frontend
 * NO FRIENDSHIP CHECK - All group members can see and join
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVideoCallDTO {

    private String id;
    private String groupId;
    private String groupName;
    private String initiatorId;
    private String initiatorName;
    private String initiatorAvatar;
    private List<ParticipantInfo> participants = new ArrayList<>();
    private String status; // ACTIVE or ENDED
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;

    /**
     * Inner class for participant information
     * Contains basic info about each participant in the call
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private String userId;
        private String userName;
        private String userAvatar;
    }
}

