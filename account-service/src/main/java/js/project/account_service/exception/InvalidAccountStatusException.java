package js.project.account_service.exception;

public class InvalidAccountStatusException extends RuntimeException{
    public InvalidAccountStatusException(String message){
        super(message);
    }
}