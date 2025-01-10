package js.project.user_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ValidateDateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {

    private String pattern;
    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        try {
            LocalDate date = LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
            return date.isBefore(LocalDate.now());
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }

    }
}
