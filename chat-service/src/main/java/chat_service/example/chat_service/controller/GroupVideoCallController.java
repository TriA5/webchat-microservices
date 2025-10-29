package chat_service.example.chat_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import chat_service.example.chat_service.dto.GroupVideoCallDTO;
import chat_service.example.chat_service.dto.GroupVideoCallSignalDTO;
import chat_service.example.chat_service.service.videocall.GroupVideoCallService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Group Video Calls
 * 
 * NO FRIENDSHIP CHECK - All group members can participate in calls
 * Example: A, B, C are in same group. A not friends with C. C can still join A's call.
 */
@RestController
@RequestMapping("/group-video-call")
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:3000")
public class GroupVideoCallController {

    private final GroupVideoCallService groupVideoCallService;

    /**
     * Initiate a new group video call
     * Sends notification to ALL group members (NO friendship check)
     */
    @PostMapping("/initiate")
    public ResponseEntity<GroupVideoCallDTO> initiateCall(@RequestBody Map<String, String> request) {
        String groupId = request.get("groupId");
        String initiatorId = request.get("initiatorId");
        
        GroupVideoCallDTO call = groupVideoCallService.initiateGroupCall(groupId, initiatorId);
        return ResponseEntity.ok(call);
    }

    /**
     * Join an existing group video call
     * NO friendship check - any group member can join
     */
    @PostMapping("/{callId}/join")
    public ResponseEntity<GroupVideoCallDTO> joinCall(
            @PathVariable String callId,
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        
        GroupVideoCallDTO call = groupVideoCallService.joinGroupCall(callId, userId);
        return ResponseEntity.ok(call);
    }

    /**
     * Leave a group video call
     */
    @PostMapping("/{callId}/leave")
    public ResponseEntity<Void> leaveCall(
            @PathVariable String callId,
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        
        groupVideoCallService.leaveGroupCall(callId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * End a group video call (only initiator can end)
     */
    @PostMapping("/{callId}/end")
    public ResponseEntity<Void> endCall(@PathVariable String callId) {
        groupVideoCallService.endGroupCall(callId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get active call for a group
     * Returns call info if there's an active call
     */
    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<GroupVideoCallDTO> getActiveCall(@PathVariable String groupId) {
        Optional<GroupVideoCallDTO> call = groupVideoCallService.getActiveCallForGroup(groupId);
        return call.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Get call history for a group
     */
    @GetMapping("/group/{groupId}/history")
    public ResponseEntity<List<GroupVideoCallDTO>> getCallHistory(@PathVariable String groupId) {
        List<GroupVideoCallDTO> history = groupVideoCallService.getCallHistoryForGroup(groupId);
        return ResponseEntity.ok(history);
    }

    /**
     * WebSocket endpoint for WebRTC signaling
     * Handles peer-to-peer signal exchange (offers, answers, ICE candidates)
     */
    @MessageMapping("/group-video-signal")
    public void handleSignal(@Payload GroupVideoCallSignalDTO signal) {
        System.out.println("📡 Received WebRTC signal: " + signal.getType() + " from " + signal.getFromUserId() + " to " + signal.getToUserId());
        groupVideoCallService.handleGroupCallSignal(signal);
    }
}

