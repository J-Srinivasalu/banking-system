package js.project.auth_service.exception;

public class AuthenticationException extends RuntimeException{
    public AuthenticationException(String message){
        super(message);
    }
}
