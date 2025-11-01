package poster_service.example.poster_service.security;

public class Endpoints {
    
    // Public endpoints - không cần authentication
    public static final String[] PUBLIC_GET = {
        "/admin/**",
        // "/gemini/**",
        "/posters/**",
        "/like-posters/**",
        "/comments/**",
        "/notifications/**"
    };
    
    public static final String[] PUBLIC_POST = {
        "/admin/**",
        "/posters/**",
        "/like-posters/**",
        "/comments/**",
        "/notifications/**"
    };
    
    public static final String[] PUBLIC_PUT = {
        "/admin/**",
        "/posters/**",
        "/comments/**",
        "/notifications/**"
    };
    
    public static final String[] PUBLIC_DELETE = {
        "/admin/**",
        "/posters/**",
        "/like-posters/**",
        "/comments/**",
        "/notifications/**"
    };
    
    // Admin endpoints - cần role ADMIN
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}


