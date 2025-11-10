package poster_service.example.poster_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poster_service.example.poster_service.entity.CommentSharePoster;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentSharePosterRepository extends JpaRepository<CommentSharePoster, UUID> {

    // Lấy tất cả comment gốc (không phải reply) của 1 share poster
    @Query("SELECT c FROM CommentSharePoster c WHERE c.sharePoster.idShare = :shareId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<CommentSharePoster> findRootCommentsByShareId(UUID shareId);

    // Lấy tất cả reply của 1 comment
    List<CommentSharePoster> findByParentComment_IdCommentShareOrderByCreatedAtAsc(UUID parentCommentId);

    // Đếm số comment của 1 share poster
    Long countBySharePoster_IdShare(UUID shareId);

    // Đếm số reply của 1 comment
    Long countByParentComment_IdCommentShare(UUID parentCommentId);

    // Xóa tất cả comment của 1 share poster
    @Modifying
    @Query("DELETE FROM CommentSharePoster c WHERE c.sharePoster.idShare = :shareId")
    void deleteAllByShareId(UUID shareId);
}
