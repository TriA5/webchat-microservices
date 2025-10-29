package chat_service.example.chat_service.service.videocall;

import chat_service.example.chat_service.client.UserClient;
import chat_service.example.chat_service.client.UserDTO;
import chat_service.example.chat_service.dto.VideoCallDTO;
import chat_service.example.chat_service.dto.VideoCallSignalDTO;
import chat_service.example.chat_service.entity.VideoCall;
import chat_service.example.chat_service.repository.VideoCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoCallService {
    
    private final VideoCallRepository videoCallRepository;
    private final UserClient userClient;
    private final SimpMessagingTemplate messagingTemplate;

    public VideoCall initiateCall(UUID callerId, UUID calleeId) {
        // Check if either user is already in an active call
        Optional<VideoCall> callerActiveCall = videoCallRepository.findActiveCallByUser(callerId);
        Optional<VideoCall> calleeActiveCall = videoCallRepository.findActiveCallByUser(calleeId);

        if (callerActiveCall.isPresent() || calleeActiveCall.isPresent()) {
            throw new RuntimeException("User is already in an active call");
        }

        VideoCall videoCall = new VideoCall();
        videoCall.setCaller(callerId);
        videoCall.setCallee(calleeId);
        videoCall.setStatus(VideoCall.CallStatus.INITIATED);
        VideoCall saved = videoCallRepository.save(videoCall);

        // Notify callee about incoming call
        VideoCallDTO callDto = convertToDTO(saved);
        messagingTemplate.convertAndSend("/topic/video-call/" + calleeId.toString(), callDto);

        return saved;
    }
    
    public void handleCallSignal(VideoCallSignalDTO signal) {
        VideoCall call = videoCallRepository.findById(signal.getCallId()).orElseThrow();

        switch (signal.getType()) {
            case CALL_OFFER:
                call.setStatus(VideoCall.CallStatus.RINGING);
                videoCallRepository.save(call);
                // Forward offer to callee
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;

            case CALL_ANSWER:
                // Forward answer to caller
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;

            case ICE_CANDIDATE:
                // Forward ICE candidate to the other peer
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;

            case CALL_ACCEPT:
                call.setStatus(VideoCall.CallStatus.ACCEPTED);
                call.setStartedAt(LocalDateTime.now());
                videoCallRepository.save(call);
                // Notify caller that call was accepted
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;

            case CALL_REJECT:
                call.setStatus(VideoCall.CallStatus.REJECTED);
                call.setEndedAt(LocalDateTime.now());
                videoCallRepository.save(call);
                // Notify caller that call was rejected
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;

            case CALL_END:
                endCall(call);
                // Notify the other party that call ended
                messagingTemplate.convertAndSend("/topic/video-signal/" + signal.getToUserId().toString(), signal);
                break;
        }
    }
    
    public void endCall(VideoCall call) {
        if (call.getStatus() == VideoCall.CallStatus.ACCEPTED && call.getStartedAt() != null) {
            LocalDateTime endTime = LocalDateTime.now();
            call.setEndedAt(endTime);
            call.setDurationSeconds(ChronoUnit.SECONDS.between(call.getStartedAt(), endTime));
        }
        call.setStatus(VideoCall.CallStatus.ENDED);
        videoCallRepository.save(call);
    }
    
    public void endCallById(UUID callId) {
        VideoCall call = videoCallRepository.findById(callId).orElseThrow();
        endCall(call);

        // Notify both parties
        VideoCallSignalDTO endSignal = new VideoCallSignalDTO();
        endSignal.setCallId(callId);
        endSignal.setType(VideoCallSignalDTO.SignalType.CALL_END);

        messagingTemplate.convertAndSend("/topic/video-signal/" + call.getCaller().toString(), endSignal);
        messagingTemplate.convertAndSend("/topic/video-signal/" + call.getCallee().toString(), endSignal);
    }
    
    public List<VideoCallDTO> getCallHistory(UUID userId) {
        return videoCallRepository.findCallHistoryByUser(userId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<VideoCallDTO> getActiveCall(UUID userId) {
        return videoCallRepository.findActiveCallByUser(userId)
                .map(this::convertToDTO);
    }
    
    public void acceptCall(UUID callId) {
        VideoCall call = videoCallRepository.findById(callId).orElseThrow();
        call.setStatus(VideoCall.CallStatus.ACCEPTED);
        call.setStartedAt(LocalDateTime.now());
        videoCallRepository.save(call);

        // Notify both parties
        VideoCallDTO callDTO = convertToDTO(call);
        messagingTemplate.convertAndSend("/topic/video-call/" + call.getCaller().toString(), callDTO);
        messagingTemplate.convertAndSend("/topic/video-call/" + call.getCallee().toString(), callDTO);
    }
    
    public void rejectCall(UUID callId) {
        VideoCall call = videoCallRepository.findById(callId).orElseThrow();
        call.setStatus(VideoCall.CallStatus.REJECTED);
        call.setEndedAt(LocalDateTime.now());
        videoCallRepository.save(call);

        // Notify both parties
        VideoCallDTO callDTO = convertToDTO(call);
        messagingTemplate.convertAndSend("/topic/video-call/" + call.getCaller().toString(), callDTO);
        messagingTemplate.convertAndSend("/topic/video-call/" + call.getCallee().toString(), callDTO);
    }
    
    private VideoCallDTO convertToDTO(VideoCall videoCall) {
        VideoCallDTO dto = new VideoCallDTO();
        dto.setId(videoCall.getId());
        dto.setStatus(videoCall.getStatus().name());
        dto.setCreatedAt(videoCall.getCreatedAt());
        dto.setStartedAt(videoCall.getStartedAt());
        dto.setEndedAt(videoCall.getEndedAt());
        dto.setDurationSeconds(videoCall.getDurationSeconds());

        // Caller info
        try {
            UserDTO caller = userClient.getUserById(videoCall.getCaller());
            dto.setCallerId(caller.getIdUser());
            dto.setCallerName((caller.getFirstName() == null ? "" : caller.getFirstName()) + " " + (caller.getLastName() == null ? "" : caller.getLastName()));
            dto.setCallerAvatar(caller.getAvatar());
        } catch (Exception ex) {
            dto.setCallerId(videoCall.getCaller());
            dto.setCallerName(videoCall.getCaller().toString());
            dto.setCallerAvatar(null);
        }

        // Callee info
        try {
            UserDTO callee = userClient.getUserById(videoCall.getCallee());
            dto.setCalleeId(callee.getIdUser());
            dto.setCalleeName((callee.getFirstName() == null ? "" : callee.getFirstName()) + " " + (callee.getLastName() == null ? "" : callee.getLastName()));
            dto.setCalleeAvatar(callee.getAvatar());
        } catch (Exception ex) {
            dto.setCalleeId(videoCall.getCallee());
            dto.setCalleeName(videoCall.getCallee().toString());
            dto.setCalleeAvatar(null);
        }

        return dto;
    }
}
