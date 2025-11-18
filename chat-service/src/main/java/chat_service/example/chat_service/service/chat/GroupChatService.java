package chat_service.example.chat_service.service.chat;

import chat_service.example.chat_service.client.AiClient;
import chat_service.example.chat_service.client.UploadClient;
import chat_service.example.chat_service.client.UserClient;
import chat_service.example.chat_service.dto.ChatMessageDTO;
import chat_service.example.chat_service.dto.GroupConversationDTO;
import chat_service.example.chat_service.dto.GroupMemberDTO;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.GroupMember;
import chat_service.example.chat_service.entity.Message;
import chat_service.example.chat_service.repository.GroupConversationRepository;
import chat_service.example.chat_service.repository.GroupMemberRepository;
import chat_service.example.chat_service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {
    private static final Logger log = LoggerFactory.getLogger(GroupChatService.class);
    private final GroupConversationRepository groupConversationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageRepository messageRepository;
    private final UploadClient uploadClient;
    private final UserClient userClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final AiClient aiClient;

    public GroupConversation createGroup(UUID creatorId, String groupName, List<UUID> initialMemberIds) {
        GroupConversation group = new GroupConversation();
        group.setName(groupName);
        group.setCreatedBy(creatorId);
        GroupConversation savedGroup = groupConversationRepository.save(group);

        // Thêm creator là ADMIN
        addMember(savedGroup, creatorId, "ADMIN");

        // Thêm initial members là MEMBER
        for (UUID memberId : initialMemberIds) {
            addMember(savedGroup, memberId, "MEMBER");
        }

        // Notify tất cả thành viên về group mới
        GroupConversationDTO dto = mapToDTO(savedGroup);
        for (GroupMember gm : groupMemberRepository.findByGroup(savedGroup)) {
            messagingTemplate.convertAndSend("/topic/groups/" + gm.getUser(), dto);
        }

        return savedGroup;
    }

    public void joinGroup(UUID groupId, UUID userId) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        if (groupMemberRepository.findByGroupAndUser(group, userId).isPresent()) {
            throw new RuntimeException("Đã là thành viên của nhóm");
        }
        addMember(group, userId, "MEMBER");

        // Notify nhóm về thành viên mới
        String username = "User";
        try {
            var u = userClient.getUserById(userId);
            if (u != null) username = u.getUsername();
        } catch (Exception ignored) {}
        messagingTemplate.convertAndSend("/topic/group/" + groupId, "User " + username + " đã tham gia");
    }
    //thêm thành viên vào nhóm khi chưa kết bạn
    public void addMemberToGroupIfNotFriend(UUID groupId, UUID userId) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        if (groupMemberRepository.findByGroupAndUser(group, userId).isPresent()) {
            throw new RuntimeException("Đã là thành viên của nhóm");
        }
        addMember(group, userId, "MEMBER");

        // Notify nhóm về thành viên mới
        String username = "User";
        try {
            var u = userClient.getUserById(userId);
            if (u != null) username = u.getUsername();
        } catch (Exception ignored) {}
        messagingTemplate.convertAndSend("/topic/group/" + groupId, "User " + username + " đã tham gia");
    }
    public ChatMessageDTO sendGroupMessage(UUID groupId, UUID senderId, String content) {
    try {
        // Kiểm tra toxic trước khi gửi tin nhắn nhóm
        if (isToxic(content)) {
            content = "***";
        }

        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

        groupMemberRepository.findByGroupAndUser(group, senderId)
                .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

        Message m = new Message();
        m.setGroupConversation(group);
        m.setSender(senderId);
        m.setContent(content);
        m.setMessageType("TEXT");
        Message saved = messageRepository.save(m);

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(saved.getId());
        dto.setConversationId(null);
        dto.setGroupId(groupId);
        dto.setSenderId(senderId);
        dto.setSenderAvatar(null);
        dto.setContent(saved.getContent());
        dto.setMessageType(saved.getMessageType());
        dto.setImageUrl(null);
        dto.setFileUrl(null);
        dto.setFileName(null);
        dto.setFileSize(null);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
        return dto;
    } catch (Exception e) {
        System.err.println("Lỗi khi gửi tin nhắn nhóm: " + e.getMessage());
        e.printStackTrace();
        throw e; // Ném lại để debug
    }
}
    public List<ChatMessageDTO> getGroupMessages(UUID groupId) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        return messageRepository.findByGroupConversationOrderByCreatedAtAsc(group)
                .stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        var u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setId(m.getId());
                    dto.setConversationId(null);
                    dto.setGroupId(groupId);
                    dto.setSenderId(m.getSender());
                    dto.setSenderAvatar(avatar);
                    dto.setContent(m.getContent());
                    dto.setMessageType(m.getMessageType());
                    dto.setImageUrl(m.getImageUrl());
                    dto.setFileUrl(m.getFileUrl());
                    dto.setFileName(m.getFileName());
                    dto.setFileSize(m.getFileSize());
                    dto.setCreatedAt(m.getCreatedAt());
                    
                    // Add validation fields from database
                    dto.setIsSexy(m.getIsSexy());
                    dto.setSexyScore(m.getSexyScore());
                    dto.setPornScore(m.getPornScore());
                    dto.setHentaiScore(m.getHentaiScore());
                    dto.setTopLabel(m.getTopLabel());
                    dto.setValidationMessage(m.getValidationMessage());
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tin nhắn nhóm với pagination
     * @param groupId ID của nhóm
     * @param page Số trang (0-based)
     * @param size Số lượng tin nhắn mỗi trang (mặc định 10)
     * @return Danh sách tin nhắn
     */
    public List<ChatMessageDTO> getGroupMessagesPaginated(UUID groupId, int page, int size) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        
        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages = messageRepository.findByGroupConversationOrderByCreatedAtDesc(group, pageable);
        
        // Đảo ngược danh sách để tin nhắn cũ nhất ở đầu
        Collections.reverse(messages);
        
        return messages.stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        var u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setId(m.getId());
                    dto.setConversationId(null);
                    dto.setGroupId(groupId);
                    dto.setSenderId(m.getSender());
                    dto.setSenderAvatar(avatar);
                    dto.setContent(m.getContent());
                    dto.setMessageType(m.getMessageType());
                    dto.setImageUrl(m.getImageUrl());
                    dto.setFileUrl(m.getFileUrl());
                    dto.setFileName(m.getFileName());
                    dto.setFileSize(m.getFileSize());
                    dto.setCreatedAt(m.getCreatedAt());
                    
                    // Add validation fields from database
                    dto.setIsSexy(m.getIsSexy());
                    dto.setSexyScore(m.getSexyScore());
                    dto.setPornScore(m.getPornScore());
                    dto.setHentaiScore(m.getHentaiScore());
                    dto.setTopLabel(m.getTopLabel());
                    dto.setValidationMessage(m.getValidationMessage());
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy tin nhắn nhóm cũ hơn một thời điểm cụ thể
     * @param groupId ID của nhóm
     * @param before Thời điểm để lấy tin nhắn trước đó
     * @param size Số lượng tin nhắn (mặc định 10)
     * @return Danh sách tin nhắn
     */
    public List<ChatMessageDTO> getGroupMessagesBeforeTimestamp(UUID groupId, LocalDateTime before, int size) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        
        Pageable pageable = PageRequest.of(0, size);
        List<Message> messages = messageRepository.findByGroupConversationBeforeTimestamp(group, before, pageable);
        
        // Đảo ngược để tin nhắn cũ nhất ở đầu
        Collections.reverse(messages);
        
        return messages.stream()
                .map(m -> {
                    String avatar = null;
                    try {
                        var u = userClient.getUserById(m.getSender());
                        if (u != null) avatar = u.getAvatar();
                    } catch (Exception ignored) {}

                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setId(m.getId());
                    dto.setConversationId(null);
                    dto.setGroupId(groupId);
                    dto.setSenderId(m.getSender());
                    dto.setSenderAvatar(avatar);
                    dto.setContent(m.getContent());
                    dto.setMessageType(m.getMessageType());
                    dto.setImageUrl(m.getImageUrl());
                    dto.setFileUrl(m.getFileUrl());
                    dto.setFileName(m.getFileName());
                    dto.setFileSize(m.getFileSize());
                    dto.setCreatedAt(m.getCreatedAt());
                    
                    // Add validation fields from database
                    dto.setIsSexy(m.getIsSexy());
                    dto.setSexyScore(m.getSexyScore());
                    dto.setPornScore(m.getPornScore());
                    dto.setHentaiScore(m.getHentaiScore());
                    dto.setTopLabel(m.getTopLabel());
                    dto.setValidationMessage(m.getValidationMessage());
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<GroupConversationDTO> getGroupsForUser(UUID userId) {
    return groupMemberRepository.findByUser(userId)
        .stream()
        .map(gm -> mapToDTO(gm.getGroup()))
        .collect(Collectors.toList());
    }

    private void addMember(GroupConversation group, UUID userId, String role) {
        GroupMember gm = new GroupMember();
        gm.setGroup(group);
        gm.setUser(userId);
        gm.setRole(role);
        groupMemberRepository.save(gm);
    }

    public void removeMember(UUID groupId, UUID userId, UUID requesterId) {
    GroupConversation group = groupConversationRepository.findById(groupId)
        .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

    // Kiểm tra quyền: phải là ADMIN hoặc chính user đó (tự rời nhóm)
    GroupMember requesterMember = groupMemberRepository.findByGroupAndUser(group, requesterId)
        .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên của nhóm"));
        
    if (!requesterId.equals(userId) && !"ADMIN".equals(requesterMember.getRole())) {
        throw new RuntimeException("Chỉ ADMIN mới có quyền xóa thành viên");
    }

    GroupMember memberToRemove = groupMemberRepository.findByGroupAndUser(group, userId)
        .orElseThrow(() -> new RuntimeException("Người dùng không phải thành viên của nhóm"));

    // Không cho phép xóa creator
    if (group.getCreatedBy().equals(userId)) {
        throw new RuntimeException("Không thể xóa người tạo nhóm");
    }

    groupMemberRepository.delete(memberToRemove);

    // Broadcast notification
    String username = "User";
    try {
        var u = userClient.getUserById(userId);
        if (u != null) username = u.getUsername();
    } catch (Exception ignored) {}

    String message = username + (requesterId.equals(userId) ? " đã rời khỏi nhóm" : " đã bị xóa khỏi nhóm");
    messagingTemplate.convertAndSend("/topic/group/" + groupId + "/member-removed", 
        new MemberRemovedNotification(groupId, userId, message));
    }

    public List<GroupMemberDTO> getGroupMembers(UUID groupId) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        
        List<GroupMember> members = groupMemberRepository.findByGroup(group);
        return members.stream()
                .map(gm -> {
                    String username = "User";
                    String avatar = null;
                    try {
                        var u = userClient.getUserById(gm.getUser());
                        if (u != null) {
                            username = u.getUsername();
                            avatar = u.getAvatar();
                        }
                    } catch (Exception ignored) {}

                    return new GroupMemberDTO(
                            gm.getId(),
                            gm.getUser(),
                            username,
                            avatar,
                            gm.getRole()
                    );
                })
                .collect(Collectors.toList());
    }

    public void deleteGroup(UUID groupId, UUID requesterId) {
        GroupConversation group = groupConversationRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));
        
        // Chỉ creator mới có thể xóa nhóm
        if (!group.getCreatedBy().equals(requesterId)) {
            throw new RuntimeException("Chỉ người tạo nhóm mới có thể xóa nhóm");
        }

        // Lấy tất cả thành viên trước khi xóa
        List<GroupMember> members = groupMemberRepository.findByGroup(group);
        List<UUID> memberIds = members.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());

        // Xóa tất cả tin nhắn trong nhóm
        List<Message> messages = messageRepository.findByGroupConversationOrderByCreatedAtAsc(group);
        messageRepository.deleteAll(messages);

        // Xóa tất cả thành viên
        groupMemberRepository.deleteAll(members);

        // Xóa nhóm
        groupConversationRepository.delete(group);

        // Thông báo cho tất cả thành viên về việc nhóm bị xóa
        GroupDeletedNotification notification = new GroupDeletedNotification(
                groupId,
                group.getName(),
                "Nhóm đã bị xóa bởi người tạo"
        );
        
        for (UUID memberId : memberIds) {
            messagingTemplate.convertAndSend("/topic/groups/" + memberId, notification);
        }
    }

    private GroupConversationDTO mapToDTO(GroupConversation group) {
        return new GroupConversationDTO(group.getId(), group.getName(), group.getCreatedBy());
    }

    // DTO for notification
    public static class MemberRemovedNotification {
        public UUID groupId;
        public UUID userId;
        public String message;
        public MemberRemovedNotification(UUID groupId, UUID userId, String message) {
            this.groupId = groupId;
            this.userId = userId;
            this.message = message;
        }
    }

    public static class GroupDeletedNotification {
        public UUID groupId;
        public String groupName;
        public String message;
        public GroupDeletedNotification(UUID groupId, String groupName, String message) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.message = message;
        }
    }

    public ChatMessageDTO sendGroupImageMessage(UUID groupId, UUID senderId, MultipartFile imageFile) {
        try {
            GroupConversation group = groupConversationRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

            groupMemberRepository.findByGroupAndUser(group, senderId)
                    .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

            // Diagnostic logging
            if (imageFile != null) {
                log.info("sendGroupImageMessage: groupId={} senderId={} filename={} size={} contentType={}",
                        groupId, senderId, imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());
            } else {
                log.warn("sendGroupImageMessage called with null imageFile: groupId={} senderId={}", groupId, senderId);
            }

            // Check image content using AI service
            Boolean isSexy = null;
            Double sexyScore = null;
            Double pornScore = null;
            Double hentaiScore = null;
            String topLabel = null;
            String validationMessage = null;
            
            try {
                String base64 = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
                String dataUri = "data:" + (imageFile.getContentType() == null ? "image/png" : imageFile.getContentType()) + ";base64," + base64;
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("image", dataUri);
                
                Map<String, Object> validationResult = aiClient.checkImageSexyBase64(requestBody);
                log.info("Image validation result: {}", validationResult);
                
                if (validationResult != null) {
                    isSexy = (Boolean) validationResult.get("is_sexy");
                    sexyScore = validationResult.get("sexy_score") != null ? 
                        ((Number) validationResult.get("sexy_score")).doubleValue() : null;
                    pornScore = validationResult.get("porn_score") != null ? 
                        ((Number) validationResult.get("porn_score")).doubleValue() : null;
                    hentaiScore = validationResult.get("hentai_score") != null ? 
                        ((Number) validationResult.get("hentai_score")).doubleValue() : null;
                    topLabel = (String) validationResult.get("top_label");
                    validationMessage = (String) validationResult.get("message");
                }
            } catch (Exception e) {
                log.warn("Failed to validate image content, allowing send: {}", e.getMessage());
                // Continue sending image even if validation fails
            }

            // Upload image via user-service (base64)
            String base64 = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
            String dataUri = "data:" + (imageFile.getContentType() == null ? "image/png" : imageFile.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "group_chat_" + UUID.randomUUID(), "data", dataUri);
            String imageUrl = uploadClient.uploadBase64(body);
            log.info("uploadClient returned imageUrl={}", imageUrl);

            // Create message
            Message m = new Message();
            m.setGroupConversation(group);
            m.setSender(senderId);
            m.setContent(""); // Empty content for image messages
            m.setMessageType("IMAGE");
            m.setImageUrl(imageUrl);
            
            // Save validation info to database
            m.setIsSexy(isSexy);
            m.setSexyScore(sexyScore);
            m.setPornScore(pornScore);
            m.setHentaiScore(hentaiScore);
            m.setTopLabel(topLabel);
            m.setValidationMessage(validationMessage);
            
            Message saved = messageRepository.save(m);

            // Fetch avatar (best-effort)
            String avatar = null;
            try {
                var u = userClient.getUserById(senderId);
                if (u != null) avatar = u.getAvatar();
            } catch (Exception ignored) {}

            // Create DTO with validation info
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setId(saved.getId());
            dto.setConversationId(null);
            dto.setGroupId(groupId);
            dto.setSenderId(senderId);
            dto.setSenderAvatar(avatar);
            dto.setContent(saved.getContent());
            dto.setMessageType(saved.getMessageType());
            dto.setImageUrl(saved.getImageUrl());
            dto.setFileUrl(null);
            dto.setFileName(null);
            dto.setFileSize(null);
            dto.setCreatedAt(saved.getCreatedAt());
            
            // Set validation fields
            dto.setIsSexy(isSexy);
            dto.setSexyScore(sexyScore);
            dto.setPornScore(pornScore);
            dto.setHentaiScore(hentaiScore);
            dto.setTopLabel(topLabel);
            dto.setValidationMessage(validationMessage);

            // Publish to group topic
            messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
            return dto;
        } catch (Exception e) {
            log.error("Failed to send group image message for groupId={} senderId={}: {}", groupId, senderId, e.toString(), e);
            throw new RuntimeException("Failed to send group image message: " + e.getMessage(), e);
        }
    }

    public ChatMessageDTO sendGroupFileMessage(UUID groupId, UUID senderId, MultipartFile file) {
        try {
            GroupConversation group = groupConversationRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Nhóm không tồn tại"));

            groupMemberRepository.findByGroupAndUser(group, senderId)
                    .orElseThrow(() -> new RuntimeException("Không phải thành viên của nhóm"));

            // Diagnostic logging
            if (file != null) {
                log.info("sendGroupFileMessage: groupId={} senderId={} filename={} size={} contentType={}",
                        groupId, senderId, file.getOriginalFilename(), file.getSize(), file.getContentType());
            } else {
                log.warn("sendGroupFileMessage called with null file: groupId={} senderId={}", groupId, senderId);
            }

            // Upload file via user-service (base64)
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + (file.getContentType() == null ? "application/octet-stream" : file.getContentType()) + ";base64," + base64;
            java.util.Map<String, String> body = java.util.Map.of("name", "group_file_" + UUID.randomUUID(), "data", dataUri);
            String fileUrl = uploadClient.uploadBase64(body);
            log.info("uploadClient returned fileUrl={}", fileUrl);

            // Create message
            Message m = new Message();
            m.setGroupConversation(group);
            m.setSender(senderId);
            m.setContent(""); // Empty content for file messages
            m.setMessageType("FILE");
            m.setFileUrl(fileUrl);
            m.setFileName(file.getOriginalFilename());
            m.setFileSize(file.getSize());
            Message saved = messageRepository.save(m);

            // Fetch avatar (best-effort)
            String avatar = null;
            try {
                var u = userClient.getUserById(senderId);
                if (u != null) avatar = u.getAvatar();
            } catch (Exception ignored) {}

            // Create DTO
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setId(saved.getId());
            dto.setConversationId(null);
            dto.setGroupId(groupId);
            dto.setSenderId(senderId);
            dto.setSenderAvatar(avatar);
            dto.setContent(saved.getContent());
            dto.setMessageType(saved.getMessageType());
            dto.setImageUrl(null);
            dto.setFileUrl(saved.getFileUrl());
            dto.setFileName(saved.getFileName());
            dto.setFileSize(saved.getFileSize());
            dto.setCreatedAt(saved.getCreatedAt());

            // Publish to group topic
            messagingTemplate.convertAndSend("/topic/group/" + groupId, dto);
            return dto;
        } catch (Exception e) {
            log.error("Failed to send group file message for groupId={} senderId={}: {}", groupId, senderId, e.toString(), e);
            throw new RuntimeException("Failed to send group file message: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra nội dung tin nhắn có toxic không
     * @param content Nội dung tin nhắn cần kiểm tra
     * @return true nếu toxic, false nếu không toxic
     */
    private boolean isToxic(String content) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", content);
            
            Map<String, Object> response = aiClient.checkToxic(body);
            
            // Kiểm tra kết quả từ AI service
            if (response != null && response.containsKey("toxic")) {
                return (Boolean) response.get("toxic");
            }
            
            // Nếu không có response hoặc lỗi, mặc định cho phép gửi tin nhắn
            return false;
        } catch (Exception e) {
            log.warn("Không thể kiểm tra toxic, cho phép tin nhắn: " + e.getMessage());
            // Nếu AI service không khả dụng, vẫn cho phép gửi tin nhắn
            return false;
        }
    }
}

