package chat_service.example.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebRTC signaling messages in group video calls
 * Used to send SDP offers/answers and ICE candidates between peers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVideoCallSignalDTO {

    private String callId;
    private String fromUserId;
    private String toUserId; // Specific peer to send signal to
    private SignalType type;
    private Object data; // Can be SDP offer/answer or ICE candidate

    public enum SignalType {
        CALL_INITIATED,   // Call has been started
        USER_JOINED,      // New user joined the call
        USER_LEFT,        // User left the call
        CALL_ENDED,       // Call has been ended
        PEER_OFFER,       // WebRTC SDP offer
        PEER_ANSWER,      // WebRTC SDP answer
        ICE_CANDIDATE     // WebRTC ICE candidate
    }
}

