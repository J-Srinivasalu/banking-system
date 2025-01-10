package js.project.user_service.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import js.project.user_service.exception.ConstraintValidationException;
import js.project.user_service.model.request.PatchUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RequestValidator {
    private final Validator validator;

    public RequestValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }


    public void validatePatchRequest(UUID userId, PatchUserRequest request) {
        log.debug("Validating update request for user ID: {}", userId);

        Set<ConstraintViolation<PatchUserRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            List<String> errors = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.toList());

            String errorMessage = String.format("Validation failed for user id: %s. Details: %s", userId, String.join(", ", errors));
            log.warn(errorMessage); // Log the full error message with details
            throw new ConstraintValidationException(errorMessage, errors);
        }

        log.debug("Update request for user ID: {} passed validation.", userId);
    }
}