package chat_service.example.chat_service.service.videocall;

import chat_service.example.chat_service.client.UserClient;
import chat_service.example.chat_service.client.UserDTO;
import chat_service.example.chat_service.dto.GroupVideoCallDTO;
import chat_service.example.chat_service.dto.GroupVideoCallSignalDTO;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.GroupMember;
import chat_service.example.chat_service.entity.GroupVideoCall;
import chat_service.example.chat_service.repository.GroupConversationRepository;
import chat_service.example.chat_service.repository.GroupMemberRepository;
import chat_service.example.chat_service.repository.GroupVideoCallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Service for Group Video Calls
 * 
 * IMPORTANT: NO FRIENDSHIP CHECK
 * - All group members can join calls regardless of friendship status
 * - Example: A friends with B, A NOT friends with C, but C can still join group call with A
 */
@Service
@RequiredArgsConstructor
public class GroupVideoCallService {

        private final GroupVideoCallRepository groupVideoCallRepository;
        private final GroupConversationRepository groupConversationRepository;
        private final GroupMemberRepository groupMemberRepository;
        private final UserClient userClient;
        private final SimpMessagingTemplate messagingTemplate;

    /**
     * Initiate a new group video call
     * Sends notification to ALL group members (NO friendship check)
     */
            @Transactional
            public GroupVideoCallDTO initiateGroupCall(String groupId, String initiatorId) {
                UUID groupUuid = UUID.fromString(groupId);
                UUID initiatorUuid = UUID.fromString(initiatorId);

                GroupConversation group = groupConversationRepository.findById(groupUuid)
                        .orElseThrow(() -> new RuntimeException("Group not found"));

                // Check if user is a member of the group
                Optional<GroupMember> membership = groupMemberRepository.findByGroupAndUser(group, initiatorUuid);
                if (membership.isEmpty()) {
                    throw new RuntimeException("User is not a member of this group");
                }

                // Check if there's already an active call
                Optional<GroupVideoCall> existingCall = groupVideoCallRepository.findActiveCallByGroup(groupUuid);
                if (existingCall.isPresent()) {
                    throw new RuntimeException("There is already an active call in this group");
                }

                // Create new call
                GroupVideoCall call = new GroupVideoCall();
                call.setGroup(group);
                call.setInitiatorId(initiatorUuid);
                call.setStatus(GroupVideoCall.CallStatus.ACTIVE);

                // Add initiator as first participant
                call.addParticipant(initiatorUuid);

                call = groupVideoCallRepository.save(call);

                // Convert to DTO
                GroupVideoCallDTO callDTO = convertToDTO(call);

                // Send notification to ALL group members (NO friendship check)
                messagingTemplate.convertAndSend(
                        "/topic/group-video-call/" + groupId,
                        new GroupVideoCallSignalDTO(
                                call.getId(),
                                initiatorId,
                                null,
                                GroupVideoCallSignalDTO.SignalType.CALL_INITIATED,
                                callDTO
                        )
                );

                return callDTO;
            }

            @Transactional
            public GroupVideoCallDTO joinGroupCall(String callId, String userId) {
                GroupVideoCall call = groupVideoCallRepository.findById(callId)
                        .orElseThrow(() -> new RuntimeException("Call not found"));

                if (call.getStatus() != GroupVideoCall.CallStatus.ACTIVE) {
                    throw new RuntimeException("Call is not active");
                }

                UUID userUuid = UUID.fromString(userId);

                // Check if user is a member of the group (only group membership check, NO friendship)
                Optional<GroupMember> membership = groupMemberRepository.findByGroupAndUser(call.getGroup(), userUuid);
                if (membership.isEmpty()) {
                    throw new RuntimeException("User is not a member of this group");
                }

                // Add participant if not already in call
                if (!call.hasParticipant(userUuid)) {
                    call.addParticipant(userUuid);
                    call = groupVideoCallRepository.save(call);
                }

                // Convert to DTO
                GroupVideoCallDTO callDTO = convertToDTO(call);

                // Notify all existing participants about the new user
                var participantInfo = buildParticipantInfo(userUuid);
                messagingTemplate.convertAndSend(
                        "/topic/group-video-call/" + call.getGroup().getId(),
                        new GroupVideoCallSignalDTO(
                                callId,
                                userId,
                                null,
                                GroupVideoCallSignalDTO.SignalType.USER_JOINED,
                                participantInfo
                        )
                );

                return callDTO;
            }

    /**
     * Leave a group video call
     */
    @Transactional
    public void leaveGroupCall(String callId, String userId) {
        GroupVideoCall call = groupVideoCallRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        UUID userUuid = UUID.fromString(userId);

        // Remove participant
        call.removeParticipant(userUuid);

        // If initiator leaves or no participants left, end the call
        if (call.getInitiatorId().equals(userUuid) || call.getParticipantCount() == 0) {
            endGroupCall(callId);
            return;
        }

        groupVideoCallRepository.save(call);

        // Notify remaining participants
        var participantInfo = buildParticipantInfo(userUuid);
        messagingTemplate.convertAndSend(
                "/topic/group-video-call/" + call.getGroup().getId(),
                new GroupVideoCallSignalDTO(
                        callId,
                        userId,
                        null,
                        GroupVideoCallSignalDTO.SignalType.USER_LEFT,
                        participantInfo
                )
        );
    }

    /**
     * End a group video call (only initiator can do this)
     */
    @Transactional
    public void endGroupCall(String callId) {
        GroupVideoCall call = groupVideoCallRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        call.endCall();
        groupVideoCallRepository.save(call);

        // Notify all participants that call has ended
        messagingTemplate.convertAndSend(
                "/topic/group-video-call/" + call.getGroup().getId(),
                new GroupVideoCallSignalDTO(
                        callId,
                        null,
                        null,
                        GroupVideoCallSignalDTO.SignalType.CALL_ENDED,
                        null
                )
        );
    }

    /**
     * Handle WebRTC signaling between peers
     * Forwards signals to specific peer
     */
    public void handleGroupCallSignal(GroupVideoCallSignalDTO signal) {
        // Send signal to specific user
        if (signal.getToUserId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/group-video-signal/" + signal.getToUserId(),
                    signal
            );
        }
    }

    /**
     * Get active call for a group
     */
    @Transactional(readOnly = true)
    public Optional<GroupVideoCallDTO> getActiveCallForGroup(String groupId) {
        GroupConversation group = groupConversationRepository.findById(java.util.UUID.fromString(groupId))
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return groupVideoCallRepository.findActiveCallByGroup(group.getId())
                .map(this::convertToDTO);
    }

    /**
     * Get call history for a group
     */
    @Transactional(readOnly = true)
    public List<GroupVideoCallDTO> getCallHistoryForGroup(String groupId) {
        GroupConversation group = groupConversationRepository.findById(java.util.UUID.fromString(groupId))
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return groupVideoCallRepository.findCallHistoryByGroup(group.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
        private GroupVideoCallDTO convertToDTO(GroupVideoCall call) {
                GroupVideoCallDTO dto = new GroupVideoCallDTO();
                dto.setId(call.getId());
                dto.setGroupId(call.getGroup().getId().toString());
                dto.setGroupName(call.getGroup().getName());

                // Initiator info (best-effort via UserClient)
                try {
                        UserDTO initiator = userClient.getUserById(call.getInitiatorId());
                        dto.setInitiatorId(initiator.getIdUser().toString());
                        dto.setInitiatorName((initiator.getFirstName() == null ? "" : initiator.getFirstName()) + " " + (initiator.getLastName() == null ? "" : initiator.getLastName()));
                        dto.setInitiatorAvatar(initiator.getAvatar());
                } catch (Exception ex) {
                        // Fallback to raw id when user-service not available
                        dto.setInitiatorId(call.getInitiatorId().toString());
                        dto.setInitiatorName(call.getInitiatorId().toString());
                        dto.setInitiatorAvatar(null);
                }

                dto.setStatus(call.getStatus().name());
                dto.setCreatedAt(call.getCreatedAt());
                dto.setEndedAt(call.getEndedAt());
                dto.setDurationSeconds(call.getDurationSeconds());

                // Convert participants (best-effort lookups)
                List<GroupVideoCallDTO.ParticipantInfo> participants = call.getParticipants()
                                .stream()
                                .map(this::buildParticipantInfo)
                                .collect(Collectors.toList());
                dto.setParticipants(participants);

                return dto;
        }

        /**
         * Build a ParticipantInfo from a user UUID by calling the user-service.
         * Falls back to minimal info when user-service is unreachable.
         */
        private GroupVideoCallDTO.ParticipantInfo buildParticipantInfo(UUID userUuid) {
                try {
                        UserDTO user = userClient.getUserById(userUuid);
                        String fullName = (user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName());
                        return new GroupVideoCallDTO.ParticipantInfo(user.getIdUser().toString(), fullName.trim(), user.getAvatar());
                } catch (Exception e) {
                        return new GroupVideoCallDTO.ParticipantInfo(userUuid.toString(), userUuid.toString(), null);
                }
        }
}

