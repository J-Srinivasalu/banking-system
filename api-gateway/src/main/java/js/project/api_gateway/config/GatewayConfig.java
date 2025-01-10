//package js.project.api_gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GatewayConfig {
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("auth-service-route", r -> r.path("/auth/**")
//                        .uri("lb://auth-service"))
//                .route("user-service-route", r -> r.path("/users/**")
//                        .filters(f -> f.filter(jwtAuthenticationFilter)) // Correct: Apply the filter instance
//                        .uri("lb://user-service"))
//                .route("account-service-route", r -> r.path("/accounts/**", "/account-approvals/**")
//                        .filters(f -> f.filter(jwtAuthenticationFilter)) // Correct: Apply the filter instance
//                        .uri("lb://account-service"))
//                .globalFilters(loggingGlobalFilter) // Apply the global filter
//                .build();
//    }
//
//}
