package chat_service.example.chat_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import chat_service.example.chat_service.entity.GroupConversation;
import chat_service.example.chat_service.entity.GroupMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// @RepositoryRestResource(path = "group-members")
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByGroup(GroupConversation group);
    List<GroupMember> findByUser(UUID user);
    Optional<GroupMember> findByGroupAndUser(GroupConversation group, UUID user);
}
