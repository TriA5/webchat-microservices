package user_service.example.user_service.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import user_service.example.user_service.entity.User;



public interface UserSecurityService extends UserDetailsService {
    public User findByUsername(String username);
}
