package js.project.auth_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import js.project.auth_service.security.JwtAuthenticationFilter;
import js.project.auth_service.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // This is the crucial line
    }

    @Test
    void doFilterInternal_missingRole_sendsErrorResponse() throws Exception {
        String jwt = "testJwt";
        String userEmail = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(jwtService.extractClaim(eq(jwt), any())).thenReturn(Optional.empty());
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        StringWriter stringWriter = new StringWriter(); // Using StringWriter as out for testing
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.debug("response (string writer content) : {}", stringWriter);
        assertThat(stringWriter.toString()).contains("Invalid access token");
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_validJwt_setsAuthentication() throws Exception {
        String jwt = "testJwt";
        String userEmail = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(jwtService.extractClaim(eq(jwt), any())).thenReturn(Optional.of("USER"));
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        log.info("Authorities: {}", authentication.getAuthorities());
        assertThat(authentication.getAuthorities()).extracting("role").containsExactlyInAnyOrder("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingAuthHeader_continuesFilterChain() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_invalidAuthHeader_continuesFilterChain() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidHeader");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }


    @Test
    void doFilterInternal_invalidJwt_sendsErrorResponse() throws Exception {
        String jwt = "invalidJwt";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenThrow(new JwtException("Invalid token"));
        StringWriter stringWriter = new StringWriter(); // Using StringWriter as out for testing
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        assertThat(stringWriter.toString()).contains("Invalid token");
//        verify(response.getWriter()).write(contains("Invalid token")); //response.getWriter returns printer writer and it is not mocked
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_exceptionInUserDetailsService_sendsInternalServerError() throws Exception {
        String jwt = "testJwt";
        String userEmail = "test@example.com";
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(userDetailsService.loadUserByUsername(userEmail)).thenThrow(new RuntimeException("User details loading failed"));

        StringWriter stringWriter = new StringWriter(); // Using StringWriter as out for testing
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        assertThat(stringWriter.toString()).contains("Error during authentication");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }


}