package poster_service.example.poster_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poster_service.example.poster_service.entity.LikeSharePoster;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeSharePosterRepository extends JpaRepository<LikeSharePoster, UUID> {

    // Tìm like của user trên share poster
    @Query("SELECT l FROM LikeSharePoster l WHERE l.sharePoster.idShare = :shareId AND l.user = :userId")
    Optional<LikeSharePoster> findByShareIdAndUserId(UUID shareId, UUID userId);

    // Kiểm tra user đã like share chưa
    boolean existsBySharePoster_IdShareAndUser(UUID shareId, UUID userId);

    // Đếm số like của 1 share poster
    Long countBySharePoster_IdShare(UUID shareId);

    // Xóa tất cả like của 1 share poster
    @Modifying
    @Query("DELETE FROM LikeSharePoster l WHERE l.sharePoster.idShare = :shareId")
    void deleteAllByShareId(UUID shareId);
}
