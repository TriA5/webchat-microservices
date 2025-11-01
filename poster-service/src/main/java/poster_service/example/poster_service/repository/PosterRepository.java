package poster_service.example.poster_service.repository;


import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import poster_service.example.poster_service.entity.Poster;

// @RepositoryRestResource(excerptProjection = User.class, path = "posters")
public interface PosterRepository extends JpaRepository<Poster, UUID> {
    List<Poster> findByUserOrderByCreatedAtDesc(UUID user);
    List<Poster> findAllByOrderByCreatedAtDesc();

    Optional<Poster> findById(UUID posterId);
}

