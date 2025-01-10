package js.project.auth_service.model;// Auth Service: UserCreatedEvent.java
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Address address; // Include the Address object
    private String nationality;
    private String nationalId;
    private String occupation;
}