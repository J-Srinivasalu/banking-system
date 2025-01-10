package js.project.auth_service.exception;

public class EmailAlreadyTakenException extends RuntimeException {
    public EmailAlreadyTakenException(String email) {
        super("Email is already taken: " + email);
    }
}