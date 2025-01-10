package js.project.auth_service.exception;

public class PhoneNumberAlreadyTakenException extends RuntimeException {
    public PhoneNumberAlreadyTakenException(String phoneNumber) {
        super("Phone number is already taken: " + phoneNumber);
    }
}