package js.project.auth_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        boolean isValid = true;

        if (password.length() < 8 || password.length() > 20) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must be 8-20 characters long.")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!Pattern.compile(".*[0-9].*").matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one digit.")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!Pattern.compile(".*[a-z].*").matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one lowercase letter.")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!Pattern.compile(".*[A-Z].*").matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one uppercase letter.")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!Pattern.compile(".*[!@#&()–[{}]:;',?/*~$^+=<>].*").matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one special character.")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}