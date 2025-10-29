package chat_service.example.chat_service.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import chat_service.example.chat_service.dto.VideoCallDTO;
import chat_service.example.chat_service.dto.VideoCallSignalDTO;
import chat_service.example.chat_service.service.videocall.VideoCallService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/video-call")
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:3000")
public class VideoCallController {
    
    private final VideoCallService videoCallService;

    @PostMapping("/initiate")
    public VideoCallDTO initiateCall(@RequestBody InitiateCallRequest request) {
        try {
            System.out.println("📞 VideoCallController.initiateCall called with: " + request.getCallerId() + " -> " + request.getCalleeId());
            videoCallService.initiateCall(request.getCallerId(), request.getCalleeId());
            // After initiating, fetch the active call DTO for the caller
            return videoCallService.getActiveCall(request.getCallerId())
                    .orElseThrow(() -> new RuntimeException("Failed to create or retrieve call"));
        } catch (Exception e) {
            System.err.println("❌ Error in initiateCall: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @PostMapping("/{callId}/end")
    public void endCall(@PathVariable UUID callId) {
        videoCallService.endCallById(callId);
    }
    
    @GetMapping("/history")
    public List<VideoCallDTO> getCallHistory(@RequestParam UUID userId) {
        return videoCallService.getCallHistory(userId);
    }
    
    @GetMapping("/active")
    public Optional<VideoCallDTO> getActiveCall(@RequestParam UUID userId) {
        return videoCallService.getActiveCall(userId);
    }
    
    @MessageMapping("/video-call.signal")
    public void handleVideoCallSignal(@Payload VideoCallSignalDTO signal) {
        videoCallService.handleCallSignal(signal);
    }
    
    @MessageMapping("/video-call/end")
    public void handleEndCall(@Payload EndCallRequest request) {
        videoCallService.endCallById(request.getCallId());
    }
    
    @MessageMapping("/video-call/accept")
    public void handleAcceptCall(@Payload AcceptCallRequest request) {
        videoCallService.acceptCall(request.getCallId());
    }
    
    @MessageMapping("/video-call/reject")
    public void handleRejectCall(@Payload RejectCallRequest request) {
        videoCallService.rejectCall(request.getCallId());
    }
    
    @Data
    public static class InitiateCallRequest {
        private UUID callerId;
        private UUID calleeId;
    }
    
    @Data
    public static class EndCallRequest {
        private UUID callId;
    }
    
    @Data
    public static class AcceptCallRequest {
        private UUID callId;
    }
    
    @Data
    public static class RejectCallRequest {
        private UUID callId;
    }
    
    // Controller no longer converts entity-to-DTO; VideoCallService provides DTOs.
}
