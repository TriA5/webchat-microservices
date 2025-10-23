package chat_service.example.chat_service.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import chat_service.example.chat_service.entity.GroupConversation;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(path = "group-conversations")
public interface GroupConversationRepository extends JpaRepository<GroupConversation, UUID> {
    List<GroupConversation> findByCreatedBy(UUID createdBy);
}
