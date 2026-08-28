package demo.reactividad.filter;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import demo.reactividad.enums.AuthenticationCategory;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class AuthenticationWebFilter implements WebFilter{

    private final Map<String, AuthenticationCategory> AUTH_CATEGORY_MAP;

    public AuthenticationWebFilter(
            @Value("${security.auth.tokens.standard}") String standardToken,
            @Value("${security.auth.tokens.prime}") String primeToken) {
        this.AUTH_CATEGORY_MAP = Map.of(
            standardToken, AuthenticationCategory.STANDARD,
            primeToken, AuthenticationCategory.PRIME
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (headers.containsHeader("auth-token")) {
            String token = headers.getFirst("auth-token");
            if(Objects.nonNull(token) && AUTH_CATEGORY_MAP.containsKey(token)){
                exchange.getAttributes().put("category", AUTH_CATEGORY_MAP.get(token));
                return chain.filter(exchange);
            }
        }
        return Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
    }
    
}
