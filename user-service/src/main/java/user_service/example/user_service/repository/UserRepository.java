package user_service.example.user_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
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

    

}

