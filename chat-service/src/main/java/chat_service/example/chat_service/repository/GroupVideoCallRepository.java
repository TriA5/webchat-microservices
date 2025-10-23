package chat_service.example.chat_service.repository;

import chat_service.example.chat_service.entity.GroupVideoCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupVideoCallRepository extends JpaRepository<GroupVideoCall, String> {

    /**
     * Find active call for a specific group by group id
     */
    @Query("SELECT c FROM GroupVideoCall c WHERE c.group.id = :groupId AND c.status = 'ACTIVE'")
    Optional<GroupVideoCall> findActiveCallByGroup(@Param("groupId") UUID groupId);

    /**
     * Find active call where user is a participant (by user id)
     */
    @Query("SELECT c FROM GroupVideoCall c JOIN c.participants p WHERE p = :userId AND c.status = 'ACTIVE'")
    Optional<GroupVideoCall> findActiveCallByParticipant(@Param("userId") UUID userId);

    /**
     * Get call history for a group by group id
     */
    @Query("SELECT c FROM GroupVideoCall c WHERE c.group.id = :groupId ORDER BY c.createdAt DESC")
    List<GroupVideoCall> findCallHistoryByGroup(@Param("groupId") UUID groupId);
}

