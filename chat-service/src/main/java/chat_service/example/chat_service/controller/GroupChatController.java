package chat_service.example.chat_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.dto.GroupConversationDTO;
import chat_service.example.chat_service.dto.GroupMemberDTO;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.service.chat.GroupChatService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
// @CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupChatController {
    private final GroupChatService groupChatService;

    @PostMapping("/create")
    public ResponseEntity<GroupConversation> createGroup(
            @RequestParam UUID creatorId,
            @RequestParam String groupName,
            @RequestBody List<UUID> initialMemberIds) {
        return ResponseEntity.ok(groupChatService.createGroup(creatorId, groupName, initialMemberIds));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> joinGroup(@PathVariable UUID groupId, @RequestParam UUID userId) {
        groupChatService.joinGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable UUID groupId,
            @RequestParam UUID senderId,
            @RequestBody String content) {
        return ResponseEntity.ok(groupChatService.sendGroupMessage(groupId, senderId, content));
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupChatService.getGroupMessages(groupId));
    }

    /**
     * Lấy tin nhắn nhóm với pagination
     * GET /groups/{groupId}/messages/paginated?page=0&size=10
     */
    @GetMapping("/{groupId}/messages/paginated")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesPaginated(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(groupChatService.getGroupMessagesPaginated(groupId, page, size));
    }

    /**
     * Lấy tin nhắn nhóm cũ hơn một thời điểm cụ thể
     * GET /groups/{groupId}/messages/before?timestamp=2024-01-01T10:00:00&size=10
     */
    @GetMapping("/{groupId}/messages/before")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesBeforeTimestamp(
            @PathVariable UUID groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(groupChatService.getGroupMessagesBeforeTimestamp(groupId, timestamp, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupConversationDTO>> getGroupsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(groupChatService.getGroupsForUser(userId));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDTO>> getGroupMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupChatService.getGroupMembers(groupId));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @RequestParam UUID requesterId) {
        groupChatService.removeMember(groupId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID groupId,
            @RequestParam UUID requesterId) {
        groupChatService.deleteGroup(groupId, requesterId);
        return ResponseEntity.ok().build();
    }
}
