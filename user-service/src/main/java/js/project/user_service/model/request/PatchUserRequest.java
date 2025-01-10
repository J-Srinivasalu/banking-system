package js.project.user_service.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import js.project.user_service.model.dto.AddressDto;
import js.project.user_service.validation.ValidDateFormat;
import lombok.Data;

@Data
public class PatchUserRequest {

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{1,4}?[-.\\s]?[0-9]{1,15}$", message = "Phone number should be valid")
    private String phoneNumber;

    @ValidDateFormat(message = "Please use dd-MM-yyyy and Date of birth must be in the past", pattern = "dd-MM-yyyy")
    private String dateOfBirth;

    private AddressDto address;

    private String nationality;

    @Size(max = 255, message = "National ID/Passport number must be less than 255 characters")
    private String nationalId;

    @Size(max = 255, message = "Occupation must be less than 255 characters")
    private String occupation;

}
