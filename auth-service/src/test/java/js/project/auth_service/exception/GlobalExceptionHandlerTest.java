package js.project.auth_service.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void testHandleEmailAlreadyTaken_throwsConflict() {
        EmailAlreadyTakenException ex = new EmailAlreadyTakenException("test@example.com already exists");
        ResponseEntity<ErrorResponse> response = handler.handleEmailAlreadyTaken(ex);
        assertErrorResponse(response, HttpStatus.CONFLICT, "Please try with a different email address", ex.getMessage());
    }

    @Test
    void testHandlePhoneNumberAlreadyTaken_throwsConflict() {
        PhoneNumberAlreadyTakenException ex = new PhoneNumberAlreadyTakenException("1234567890 already exists");
        ResponseEntity<ErrorResponse> response = handler.handlePhoneNumberAlreadyTaken(ex);
        assertErrorResponse(response, HttpStatus.CONFLICT, "Please try with a different phone number", ex.getMessage());
    }

    @Test
    void testHandleUserNotFoundException_throwsNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User with email test@example.com not found");
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(ex);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "User not found with provided email", ex.getMessage());
    }

    @Test
    void testHandleInvalidCredentialException_throwsUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid password");
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialException(ex);
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "User is not authorized", ex.getMessage());
    }

    @Test
    void testHandleAuthenticationException_throwsUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Authentication failed");
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "User is not authorized", ex.getMessage());
    }

    @Test
    void testHandleValidationExceptions_throwsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("objectName", "fieldName1", "Error message 1");
        FieldError fieldError2 = new FieldError("objectName", "fieldName2", "Error message 2");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Validation failed", List.of("Error message 1", "Error message 2"));
    }

    @Test
    void testHandleRuntimeException_throwsInternalServerError() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex);
        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, please try again later", ex.getMessage());
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus, String expectedMessage, String expectedDetails) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBody().getDetails()).isEqualTo(expectedDetails);
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus, String expectedMessage, List<String> expectedDetails) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBody().getDetails()).isEqualTo(expectedDetails);
    }
}