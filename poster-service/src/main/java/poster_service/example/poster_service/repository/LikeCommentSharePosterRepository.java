package poster_service.example.poster_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poster_service.example.poster_service.entity.LikeCommentSharePoster;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeCommentSharePosterRepository extends JpaRepository<LikeCommentSharePoster, UUID> {

    // Tìm like của user trên comment share
    @Query("SELECT l FROM LikeCommentSharePoster l WHERE l.commentSharePoster.idCommentShare = :commentId AND l.user = :userId")
    Optional<LikeCommentSharePoster> findByCommentIdAndUserId(UUID commentId, UUID userId);

    // Kiểm tra user đã like comment chưa
    boolean existsByCommentSharePoster_IdCommentShareAndUser(UUID commentId, UUID userId);

    // Đếm số like của 1 comment
    Long countByCommentSharePoster_IdCommentShare(UUID commentId);

    // Xóa tất cả like của 1 comment
    @Modifying
    @Query("DELETE FROM LikeCommentSharePoster l WHERE l.commentSharePoster.idCommentShare = :commentId")
    void deleteAllByCommentId(UUID commentId);
}
