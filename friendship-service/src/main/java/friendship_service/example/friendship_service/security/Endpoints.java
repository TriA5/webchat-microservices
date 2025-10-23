package friendship_service.example.friendship_service.security;

public class Endpoints {
    
    // Public endpoints - không cần authentication
    public static final String[] PUBLIC_GET = {
        "/friendships/hello",
        "/friendships/search",
        "/friendships/*/friends",
        "/friendships/*/friendships",
        "/friendships/*/pending",
        "/friendships/*/count"
    };
    
    public static final String[] PUBLIC_POST = {
        "/friendships/send",
        "/friendships/*/respond"
    };
    
    public static final String[] PUBLIC_PUT = {
        "/admin/**"
    };
    
    public static final String[] PUBLIC_DELETE = {
        "/friendships/unfriend"
    };
    
    // Admin endpoints - cần role ADMIN
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}
