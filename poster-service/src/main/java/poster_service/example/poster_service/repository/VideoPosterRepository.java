package poster_service.example.poster_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poster_service.example.poster_service.entity.Poster;
import poster_service.example.poster_service.entity.VideoPoster;

public interface VideoPosterRepository extends JpaRepository<VideoPoster, UUID> {
    
    List<VideoPoster> findByPoster(Poster poster);
    
    @Modifying
    @Query("DELETE FROM VideoPoster v WHERE v.poster.idPoster = :posterId")
    void deleteAllByPosterId(@Param("posterId") UUID posterId);
}
