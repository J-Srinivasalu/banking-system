package js.project.auth_service.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import js.project.auth_service.model.Address;
import js.project.auth_service.validation.ValidDateFormat;
import js.project.auth_service.validation.ValidPassword;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 1, max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9]{1,4}?[-.\\s]?[0-9]{1,15}$", message = "Phone number should be valid")
    private String phoneNumber;

    @NotNull(message = "Date of birth cannot be empty")
    @ValidDateFormat(message = "Please use dd-MM-yyyy and Date of birth must be in the past", pattern = "dd-MM-yyyy")
    private String dateOfBirth;

    @Valid // Important: Validate the nested Address object
    @NotNull(message = "Address cannot be null")
    private Address address;

    @NotEmpty(message = "Password cannot be empty")
    @ValidPassword(message = "Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String password;

    @NotEmpty(message = "Nationality cannot be empty")
    private String nationality;

    @Size(max = 255, message = "National ID/Passport number must be less than 255 characters")
    private String nationalId;

    @Size(max = 255, message = "Occupation must be less than 255 characters")
    private String occupation;

}