package js.project.account_service.exception;

public class AccountAlreadyReviewedException extends RuntimeException{
    public AccountAlreadyReviewedException(String message){
        super(message);
    }
}