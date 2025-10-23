package friendship_service.example.friendship_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import friendship_service.example.friendship_service.entity.Friendship;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    
    // Kiểm tra xem đã có quan hệ giữa 2 user chưa (bất kể ai là requester)
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requesterId = :userId1 AND f.addresseeId = :userId2) OR " +
           "(f.requesterId = :userId2 AND f.addresseeId = :userId1)")
    Optional<Friendship> findByTwoUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
    
    // Kiểm tra quan hệ với direction cụ thể
    Optional<Friendship> findByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);
    
    // Lấy tất cả lời mời đến của 1 user theo status
    List<Friendship> findByAddresseeIdAndStatus(UUID addresseeId, String status);
    
    // Lấy tất cả lời mời đã gửi của 1 user theo status
    List<Friendship> findByRequesterIdAndStatus(UUID requesterId, String status);
    
    // Lấy tất cả quan hệ của 1 user (là requester hoặc addressee) theo status
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = :status")
    List<Friendship> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
    
    // Lấy tất cả quan hệ của 1 user (bất kể status)
    @Query("SELECT f FROM Friendship f WHERE f.requesterId = :userId OR f.addresseeId = :userId")
    List<Friendship> findByUserId(@Param("userId") UUID userId);
    
    // Lấy danh sách bạn bè (status = ACCEPTED) của 1 user
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriendsByUserId(@Param("userId") UUID userId);
    
    // Đếm số bạn bè của 1 user
    @Query("SELECT COUNT(f) FROM Friendship f WHERE " +
           "(f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'ACCEPTED'")
    Long countFriendsByUserId(@Param("userId") UUID userId);
}

