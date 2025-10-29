package poster_service.example.poster_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import poster_service.example.poster_service.entity.PrivacyStatusPoster;


public interface PrivacyStatusPosterRepository extends JpaRepository<PrivacyStatusPoster, UUID> {
    Optional<PrivacyStatusPoster> findByName(String name);
}

