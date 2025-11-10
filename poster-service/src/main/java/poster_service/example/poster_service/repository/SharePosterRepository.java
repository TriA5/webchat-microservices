package poster_service.example.poster_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poster_service.example.poster_service.entity.SharePoster;

import java.util.List;
import java.util.UUID;

@Repository
public interface SharePosterRepository extends JpaRepository<SharePoster, UUID> {

    // Lấy tất cả share poster của 1 user
    List<SharePoster> findByUserOrderByCreatedAtDesc(UUID userId);

    // Lấy tất cả share poster của 1 poster gốc
    List<SharePoster> findByPoster_IdPosterOrderByCreatedAtDesc(UUID posterId);

    // Lấy tất cả share poster
    List<SharePoster> findAllByOrderByCreatedAtDesc();

    // Đếm số lần 1 poster được share
    Long countByPoster_IdPoster(UUID posterId);

    // Kiểm tra user đã share poster này chưa
    boolean existsByUserAndPoster_IdPoster(UUID userId, UUID posterId);

    // Xóa tất cả share của 1 poster (khi poster gốc bị xóa)
    @Modifying
    @Query("DELETE FROM SharePoster sp WHERE sp.poster.idPoster = :posterId")
    void deleteAllByPosterId(UUID posterId);
}
