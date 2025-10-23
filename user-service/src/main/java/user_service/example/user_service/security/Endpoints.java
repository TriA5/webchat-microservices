package user_service.example.user_service.security;

public class Endpoints {
    public static final String font_end_host = "http://localhost:3000";
    public static final String[] PUBLIC_GET = {
        "/users",
        "/users/**",
        "/users/active-account",
        "/users/search/findByIdUser?IdUser={IdUser}",
    }; 
    public static final String[] PUBLIC_POST = {
        "/users/register",
        "/users/authenticate",
    };
    public static final String[] PUBLIC_PUT = {
        "/users/change-avatar",
        "/users/update-profile",
        "/users/forgot-password",
        "/users/change-password",
    };
    public static final String[] PUBLIC_DELETE = {
        "/admin/**"
    };
    public static final String[] ADMIN_ENDPOINT = {
        "/admin/**"
    };
}

