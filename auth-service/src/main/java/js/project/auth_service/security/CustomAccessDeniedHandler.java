package js.project.auth_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import js.project.auth_service.exception.ErrorResponse; // Import your ErrorResponse
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// work in progress
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = new ErrorResponse( // Use your ErrorResponse
                HttpStatus.FORBIDDEN.getReasonPhrase(), // Error
                accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Forbidden", // Message
                request.getServletPath() // path
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}