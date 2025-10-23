package chat_service.example.chat_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import chat_service.example.chat_service.entity.Conversation;
import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    List<Message> findByGroupConversationOrderByCreatedAtAsc(GroupConversation groupConversation);
}

