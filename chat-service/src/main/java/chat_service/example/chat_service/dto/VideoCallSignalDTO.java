package chat_service.example.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallSignalDTO {
    private UUID callId;
    private UUID fromUserId;
    private UUID toUserId;
    private SignalType type;
    private Object data; // Can contain SDP offer/answer or ICE candidate

    public enum SignalType {
        CALL_OFFER,      // Initiate call with SDP offer
        CALL_ANSWER,     // Answer call with SDP answer
        ICE_CANDIDATE,   // Exchange ICE candidates
        CALL_ACCEPT,     // Callee accepts the call
        CALL_REJECT,     // Callee rejects the call
        CALL_END,        // Either party ends the call
        CALL_TIMEOUT     // Call timed out
    }
}
