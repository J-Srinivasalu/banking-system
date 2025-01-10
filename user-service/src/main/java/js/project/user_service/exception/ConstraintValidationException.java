package js.project.user_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConstraintValidationException extends RuntimeException{
    String message;
    List<String> details;
}
