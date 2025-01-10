package js.project.account_service.exception;

public class AccountApprovalNotFoundException extends RuntimeException{
    public AccountApprovalNotFoundException(String message){
        super(message);
    }
}