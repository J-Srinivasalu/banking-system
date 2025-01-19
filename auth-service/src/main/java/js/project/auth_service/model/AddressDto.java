package js.project.auth_service.model;// Auth Service: Address.java (DTO)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be less than 255 characters")
    private String street;

    @Size(max = 255, message = "Apartment/Suite must be less than 255 characters")
    private String apartmentSuite;

    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City must be less than 255 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 255, message = "State must be less than 255 characters")
    private String state;

    @NotBlank(message = "Zip code is required")
    @Size(max = 20, message = "Zip code must be less than 20 characters")
    private String zipCode;

    @NotBlank(message = "Country is required")
    @Size(max = 255, message = "Country must be less than 255 characters")
    private String country;
}