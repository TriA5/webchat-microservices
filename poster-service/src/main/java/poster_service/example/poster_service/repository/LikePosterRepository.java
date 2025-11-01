package poster_service.example.poster_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poster_service.example.poster_service.entity.LikePoster;

public interface LikePosterRepository extends JpaRepository<LikePoster, UUID> {
    
    @Query("SELECT lp FROM LikePoster lp WHERE lp.poster.idPoster = :posterId AND lp.idUser = :userId")
    Optional<LikePoster> findByPosterIdAndUserId(@Param("posterId") UUID posterId, @Param("userId") UUID userId);
    
    @Query("SELECT CASE WHEN COUNT(lp) > 0 THEN true ELSE false END FROM LikePoster lp WHERE lp.poster.idPoster = :posterId AND lp.idUser = :userId")
    boolean existsByPosterIdAndUserId(@Param("posterId") UUID posterId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(lp) FROM LikePoster lp WHERE lp.poster.idPoster = :posterId")
    long countLikesByPosterId(@Param("posterId") UUID posterId);
    
}
