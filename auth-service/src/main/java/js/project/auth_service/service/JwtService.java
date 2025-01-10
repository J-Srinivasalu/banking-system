package js.project.auth_service.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import js.project.auth_service.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    private final String jwtSecret;

    private final long jwtExpirationTime;

    private final long refreshJwtExpirationTime;

    public JwtService(@Value("${jwt.secret}") String jwtSecret,
                      @Value("${jwt.accessTokenExpirationTime}") long jwtExpirationTime,
                      @Value("${jwt.refreshTokenExpirationTime}") long refreshJwtExpirationTime) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationTime = jwtExpirationTime;
        this.refreshJwtExpirationTime = refreshJwtExpirationTime;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject).orElse(null);
    }

    public <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return Optional.ofNullable(claimsResolver.apply(claims));
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
//        } catch (ExpiredJwtException ex) {
//            log.warn("Token has expired: {}", ex.getMessage());
//            throw new ExpiredJwtException(ex.getHeader(), ex.getClaims(), ex.getMessage());
//        } catch (MalformedJwtException ex) {
//            log.warn("Token is malformed: {}", ex.getMessage());
//            return null;
//        } catch (UnsupportedJwtException ex) {
//            log.warn("Token is unsupported: {}", ex.getMessage());
//            return null;
//        } catch (IllegalArgumentException ex) {
//            log.warn("Illegal argument provided for token: {}", ex.getMessage());
//            return null;
        } catch (JwtException ex) {
            log.error("Error during token validation: {}",ex.getMessage());
            throw new JwtException(ex.getMessage());
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRole());
        return generateToken(claims, user);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            User user
    ) {
        return buildToken(extraClaims, user, jwtExpirationTime);
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshJwtExpirationTime);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        try{
            return Jwts
                    .builder()
                    .claims(extraClaims)
                    .subject(user.getEmail())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSignInKey())
                    .compact();
        }catch (Exception e){
            log.error("Error building token: {}", e.getMessage());
            return null;
        }

    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}