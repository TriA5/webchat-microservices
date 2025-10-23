package chat_service.example.chat_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import chat_service.example.chat_service.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("SELECT c FROM Conversation c WHERE (c.participant1 = :u1 AND c.participant2 = :u2) OR (c.participant1 = :u2 AND c.participant2 = :u1)")
    Optional<Conversation> findBetween(UUID u1, UUID u2);

    List<Conversation> findByParticipant1OrParticipant2(UUID p1, UUID p2);
}

