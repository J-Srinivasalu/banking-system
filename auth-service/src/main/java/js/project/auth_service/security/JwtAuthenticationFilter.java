package js.project.auth_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import js.project.auth_service.exception.ErrorResponse;
import js.project.auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Authorization Header: {}", authHeader);

        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found. Continuing filter chain.");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            jwt = authHeader.substring(7);
            log.debug("Extracted JWT: {}", jwt);

            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted User Email: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("Loaded UserDetails: {}", userDetails.getUsername());

                Optional<String> role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                log.debug("Extracted Role: {}", role);

                if (role.isPresent()) {
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.get()));
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set for user: {}", userEmail);
                } else {
                    log.warn("No role found in JWT for user: {}", userEmail);
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid access token", "Invalid token: Access token required");
                    return;
                }
            }
        }
        catch (JwtException e){
            log.error("Jwt token error: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED,"Jwt token error", e.getMessage());
            return;
        }
        catch (Exception e) {
            log.error("Error during authentication: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,"Error during authentication", e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response,HttpStatus status,String message, String details) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status.value());
        ErrorResponse errorResponse = new ErrorResponse(
                status.getReasonPhrase(),
                message,
                details
        );
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}