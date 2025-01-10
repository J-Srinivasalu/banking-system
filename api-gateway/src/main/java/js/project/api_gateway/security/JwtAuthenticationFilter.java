package js.project.api_gateway.security;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import js.project.api_gateway.model.ErrorResponse;
import js.project.api_gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing Authorization header");
        }

        String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

        if (!authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization header format");
        }

        String token = authHeader.substring(7);

        try {
            UUID userId = jwtService.extractUserId(token);

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-USER-ID", String.valueOf(userId))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Invalid JWT token");
        }

    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        ErrorResponse errorResponse = new ErrorResponse(
                status.getReasonPhrase(),
                err
        );
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response to JSON", e);
            ErrorResponse genericError = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Internal Server Error");
            try {
                byte[] genericBytes = objectMapper.writeValueAsBytes(genericError);
                DataBuffer genericBuffer = response.bufferFactory().wrap(genericBytes);
                return response.writeWith(Mono.just(genericBuffer));
            } catch (JsonProcessingException ex) {
                log.error("Error serializing generic error response to JSON", ex);
                return response.setComplete();
            }
        }
    }

}