package js.project.auth_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateDateFormatValidator.class)
@Documented
public @interface ValidDateFormat {
    String message() default "Invalid date format. Please use yyyy-MM-dd";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String pattern() default "yyyy-MM-dd"; //Default Format
}
