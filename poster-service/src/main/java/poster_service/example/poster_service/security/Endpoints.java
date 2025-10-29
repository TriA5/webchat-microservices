package poster_service.example.poster_service.security;

public class Endpoints {
    
    // Public endpoints - không cần authentication
    public static final String[] PUBLIC_GET = {
        "/admin/**",
        // "/gemini/**",
        "/posters/**"
    };
    
    public static final String[] PUBLIC_POST = {
        "/admin/**",
        "/posters/**"
    };
    
    public static final String[] PUBLIC_PUT = {
        "/admin/**",
        "/posters/**"
    };
    
    public static final String[] PUBLIC_DELETE = {
        "/admin/**",
        "/posters/**"
    };
    
    // Admin endpoints - cần role ADMIN
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}


