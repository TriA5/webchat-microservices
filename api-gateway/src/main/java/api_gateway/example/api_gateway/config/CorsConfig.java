package api_gateway.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow the React dev server origin. Adjust for production accordingly.
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000","http://localhost:3001"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * Some downstream services may also add CORS response headers. When both
     * gateway and service add the same header the browser sees duplicate
     * comma-separated values which is invalid. This filter deduplicates the
     * Access-Control-Allow-Origin header by keeping the first value.
     */
    @Bean
    public GlobalFilter dedupeCorsResponseHeaderFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            var values = headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            if (values != null && values.size() > 1) {
                // keep the first value only
                headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, values.get(0));
            }
        }));
    }
}
