package js.project.auth_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
    String message() default "Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}