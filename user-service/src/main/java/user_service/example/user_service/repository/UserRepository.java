package user_service.example.user_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import user_service.example.user_service.entity.User;



@RepositoryRestResource(excerptProjection = User.class, path = "users")
public interface UserRepository extends JpaRepository<User, UUID> {
    public User findByUsername(String username);

    public boolean existsByUsername(String username);

    public boolean existsByEmail(String email);

    public boolean existsByPhoneNumber(String phoneNumber);

    public User findByEmail(String email);

    public User findByIdUser(UUID IdUser);

    Optional<User> findByPhoneNumberAndEnabledTrue(String phoneNumber);

    List<User> findByEnabledTrue();

    //Tính tông số users được kích hoạt
    @Query("SELECT COUNT(u) FROM User u")
    Long countUser();
    //Số người dùng có active true
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = true")
    Long countStatusUsers();
    
    // Số lượng người dùng đăng ký trong tháng hiện tại
    @Query("SELECT COUNT(u) FROM User u WHERE YEAR(u.createdAt) = :year AND MONTH(u.createdAt) = :month")
    Long countUsersByMonth(@Param("year") int year, @Param("month") int month);
    
    // Số lượng người dùng đăng ký trong 6 tháng gần nhất
    @Query("""
        SELECT YEAR(u.createdAt) as year, MONTH(u.createdAt) as month, COUNT(u) as count
        FROM User u
        WHERE u.createdAt >= :startDate
        GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
        ORDER BY YEAR(u.createdAt) DESC, MONTH(u.createdAt) DESC
    """)
    List<Object[]> countUsersByLast6Months(@Param("startDate") LocalDateTime startDate);

}
