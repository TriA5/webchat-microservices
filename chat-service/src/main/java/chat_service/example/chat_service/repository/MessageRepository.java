package chat_service.example.chat_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import chat_service.example.chat_service.entity.Conversation;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    List<Message> findByGroupConversationOrderByCreatedAtAsc(GroupConversation groupConversation);
    
    // Pagination: Lấy tin nhắn mới nhất theo thời gian giảm dần (DESC)
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.createdAt DESC")
    List<Message> findByConversationOrderByCreatedAtDesc(@Param("conversation") Conversation conversation, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.groupConversation = :groupConversation ORDER BY m.createdAt DESC")
    List<Message> findByGroupConversationOrderByCreatedAtDesc(@Param("groupConversation") GroupConversation groupConversation, Pageable pageable);
    
    // Lấy tin nhắn cũ hơn một thời điểm cụ thể (để load thêm khi scroll lên)
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.createdAt < :before ORDER BY m.createdAt DESC")
    List<Message> findByConversationBeforeTimestamp(@Param("conversation") Conversation conversation, @Param("before") LocalDateTime before, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.groupConversation = :groupConversation AND m.createdAt < :before ORDER BY m.createdAt DESC")
    List<Message> findByGroupConversationBeforeTimestamp(@Param("groupConversation") GroupConversation groupConversation, @Param("before") LocalDateTime before, Pageable pageable);

    //Lấy tất cã các ảnh theo id cuộc trò chuyện
    @Query("""
        SELECT m 
        FROM Message m 
        JOIN m.conversation c 
        WHERE m.messageType = 'IMAGE' 
          AND c.id = :conversationId
    """)
    List<Message> findImagesByConversation(@Param("conversationId") UUID conversationId);
    //Lấy tất cã các ảnh theo id cuộc trò chuyện
    @Query("""
        SELECT m 
        FROM Message m 
        JOIN m.conversation c 
        WHERE m.messageType = 'FILE' 
          AND c.id = :conversationId
    """)
    List<Message> findFilesByConversation(@Param("conversationId") UUID conversationId);
    
    // Đếm tất cả tin nhắn trong ngày hôm nay
    @Query("SELECT COUNT(m) FROM Message m WHERE DATE(m.createdAt) = CURRENT_DATE")
    Long countMessagesToday();
    
    // Đếm tin nhắn theo từng ngày trong tuần (7 ngày gần nhất)
    @Query("""
        SELECT DAYOFWEEK(m.createdAt) as dayOfWeek, COUNT(m) as count
        FROM Message m
        WHERE m.createdAt >= :startOfWeek
        GROUP BY DAYOFWEEK(m.createdAt)
        ORDER BY DAYOFWEEK(m.createdAt)
    """)
    List<Object[]> countMessagesByDayOfWeek(@Param("startOfWeek") LocalDateTime startOfWeek);
}

