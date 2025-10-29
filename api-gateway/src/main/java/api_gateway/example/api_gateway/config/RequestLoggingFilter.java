package api_gateway.example.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Simple diagnostic filter to log incoming request paths and Authorization header.
 * Useful to confirm whether the gateway receives the Authorization header and
 * to help debug why some proxied requests return 401/404.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        try {
            String path = exchange.getRequest().getURI().getPath();
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
            log.info("Incoming request path={} authPresent={}", path, (auth != null));
            if (auth != null) {
                log.debug("Authorization header (truncated)={}", auth.length() > 40 ? auth.substring(0, 40) + "..." : auth);
            }
        } catch (Exception ex) {
            log.warn("Failed to log request headers", ex);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // run early
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
