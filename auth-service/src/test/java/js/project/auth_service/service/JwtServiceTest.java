package js.project.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import js.project.auth_service.model.Role;
import js.project.auth_service.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private static final String SECRET_KEY = "413F4428472B4B6250655368566D59703373667639792F423F4528482B4D6251"; // test key
    private static final long JWT_EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final long REFRESH_EXPIRATION = 86400000; // 1 day in milliseconds


    @Test
    void extractUsername_validToken_returnsUsername() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void extractUsername_invalidToken_throwsJwtException() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        String invalidToken = "invalid_token";

        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractClaim_validToken_returnsClaim() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");
        String token = jwtService.generateToken(user);

        Optional<String> username = jwtService.extractClaim(token, Claims::getSubject);

        assertThat(username).isPresent().contains("test@example.com");
    }

    @Test
    void extractClaim_invalidToken_throwsJwtException() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        String invalidToken = "invalid_token";

        assertThatThrownBy(() -> jwtService.extractClaim(invalidToken, Claims::getSubject))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_validUser_returnsToken() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        user.setId(UUID.randomUUID());
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_withExtraClaims_returnsTokenWithClaims() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("testClaim", "testValue");

        String token = jwtService.generateToken(extraClaims, user);
        Optional<String> extractedClaim = jwtService.extractClaim(token, claims -> claims.get("testClaim", String.class));

        assertThat(token).isNotBlank();
        assertThat(extractedClaim).isPresent().contains("testValue");
    }

    @Test
    void generateRefreshToken_validUser_returnsRefreshToken() {
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");

        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void buildToken_exception_returnsNull(){
        JwtService jwtService = new JwtService(SECRET_KEY, JWT_EXPIRATION, REFRESH_EXPIRATION);
        User user = new User();
        user.setEmail("test@example.com");
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "invalid-secret");
        String token = jwtService.generateToken(user);
        assertThat(token).isNull();
    }
}