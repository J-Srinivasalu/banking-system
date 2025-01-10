package js.project.user_service.model.dto;

import js.project.user_service.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String nationality;
    private String nationalId;
    private String occupation;
    private AddressDto address; // Use the AddressDto

    public UserDto(User user) {
        if (user != null) {
            this.id = user.getUserId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.dateOfBirth = user.getDateOfBirth();
            this.nationality = user.getNationality();
            this.nationalId = user.getNationalId();
            this.occupation = user.getOccupation();
            if (user.getAddress() != null) {
                this.address = new AddressDto(user.getAddress());
            }
        }
    }

}