package poster_service.example.poster_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poster_service.example.poster_service.entity.CommentPoster;

public interface CommentPosterRepository extends JpaRepository<CommentPoster, UUID> {
    
    // Lấy tất cả comment gốc (không phải reply) của một poster
    @Query("SELECT c FROM CommentPoster c WHERE c.poster.idPoster = :posterId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<CommentPoster> findRootCommentsByPosterId(@Param("posterId") UUID posterId);
    
    // Lấy tất cả replies của một comment
    @Query("SELECT c FROM CommentPoster c WHERE c.parentComment.idComment = :parentCommentId ORDER BY c.createdAt ASC")
    List<CommentPoster> findRepliesByParentCommentId(@Param("parentCommentId") UUID parentCommentId);
    
    // Đếm số comment của một poster (không bao gồm replies)
    @Query("SELECT COUNT(c) FROM CommentPoster c WHERE c.poster.idPoster = :posterId AND c.parentComment IS NULL")
    long countRootCommentsByPosterId(@Param("posterId") UUID posterId);
    
    // Đếm tổng số comment và replies của một poster
    @Query("SELECT COUNT(c) FROM CommentPoster c WHERE c.poster.idPoster = :posterId")
    long countTotalCommentsByPosterId(@Param("posterId") UUID posterId);
}
