package user_service.example.user_service.repository;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import user_service.example.user_service.entity.Role;


@RepositoryRestResource(path = "roles")
public interface RoleRepository extends JpaRepository<Role, UUID> {
    public Role findByNameRole(String nameRole);
}

