package api_gateway.example.api_gateway.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromResource;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class HomeController {

    @Bean
    public RouterFunction<ServerResponse> indexRouter() {
        ClassPathResource indexHtml = new ClassPathResource("static/index.html");
        
        return RouterFunctions.route(GET("/"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/login"),
                request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(fromResource(indexHtml)))
            .andRoute(GET("/home"), 
                request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(fromResource(indexHtml)))
            .andRoute(GET("/forgot-password"), 
                request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(fromResource(indexHtml)))
            .andRoute(GET("/register"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/chat"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/chat/**"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/video-call/**"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/active"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/active/**"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/user/active-account"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)))
            .andRoute(GET("/user/active-account/**"), 
                request -> ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fromResource(indexHtml)));
    }
}

