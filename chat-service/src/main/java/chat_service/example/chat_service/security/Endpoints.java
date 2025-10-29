package chat_service.example.chat_service.security;

public class Endpoints {
    
    // Public endpoints - không cần authentication
    public static final String[] PUBLIC_GET = {
        "/ws/**",
        "/chats/**",
        "/video-call/**",
        "/group-video-call/**",
        "/users/search/findByIdUser?IdUser={IdUser}",
         "/groups/**",
         //  "/group-members/**",
        // "/group-conversations/**",
    };
    
    public static final String[] PUBLIC_POST = {
        "/ws/**",
        "/chats/**",
        "/video-call/**",
        "/group-video-call/**",
         "/groups/**",
    };
    
    public static final String[] PUBLIC_PUT = {
        "/admin/**"
    };
    
    public static final String[] PUBLIC_DELETE = {
        "/groups/**",
    };
    
    // Admin endpoints - cần role ADMIN
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}

