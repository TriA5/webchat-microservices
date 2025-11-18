package user_service.example.user_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    //số lượng người dùng tăng trưởng so với thangs trước
    

}

