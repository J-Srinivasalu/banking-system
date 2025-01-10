package js.project.user_service.exception;

import lombok.Data;

@Data
public class ErrorResponse {

    private String error;
    private String message;
    private Object details; // bad practice, Todo: use specific

    public ErrorResponse(String error, String message, Object details) {
        this.error = error;
        this.message = message;
        this.details = details;
    }

}
