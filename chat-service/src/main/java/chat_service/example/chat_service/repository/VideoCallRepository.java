package chat_service.example.chat_service.repository;

import chat_service.example.chat_service.entity.VideoCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoCallRepository extends JpaRepository<VideoCall, UUID> {

    @Query("SELECT vc FROM VideoCall vc WHERE (vc.caller = :userId OR vc.callee = :userId) AND vc.status IN ('INITIATED', 'RINGING', 'ACCEPTED')")
    Optional<VideoCall> findActiveCallByUser(@Param("userId") UUID userId);

    @Query("SELECT vc FROM VideoCall vc WHERE vc.callee = :calleeId AND vc.status = 'RINGING'")
    List<VideoCall> findIncomingCallsByCallee(@Param("calleeId") UUID calleeId);

    @Query("SELECT vc FROM VideoCall vc WHERE (vc.caller = :userId OR vc.callee = :userId) ORDER BY vc.createdAt DESC")
    List<VideoCall> findCallHistoryByUser(@Param("userId") UUID userId);
}
