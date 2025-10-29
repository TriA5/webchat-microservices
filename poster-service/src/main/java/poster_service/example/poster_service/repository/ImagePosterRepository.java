package poster_service.example.poster_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import poster_service.example.poster_service.entity.ImagePoster;
import poster_service.example.poster_service.entity.Poster;


public interface ImagePosterRepository extends JpaRepository<ImagePoster, UUID> {
    List<ImagePoster> findByPoster(Poster poster);
}

