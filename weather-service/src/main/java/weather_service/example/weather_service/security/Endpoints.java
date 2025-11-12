package weather_service.example.weather_service.security;

public class Endpoints {
    
    // Public endpoints - không cần authentication
    public static final String[] PUBLIC_GET = {
        "/admin/**",
        "/weathers/**"
    };
    
    public static final String[] PUBLIC_POST = {
        "/admin/**"
    };
    
    public static final String[] PUBLIC_PUT = {
        "/admin/**"
    };
    
    public static final String[] PUBLIC_DELETE = {
        "/admin/**"
    };
    
    // Admin endpoints - cần role ADMIN
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}


