package js.project.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_service_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must be less than 255 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must be less than 255 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Column(name = "phone_number")
    @Pattern(regexp = "^\\+?[0-9]{1,4}?[-.\\s]?[0-9]{1,15}$", message = "Phone number should be valid")
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Nationality must be less than 255 characters")
    @Column(name = "nationality")
    private String nationality;

    @Size(max = 255, message = "National ID/Passport number must be less than 255 characters")
    @Column(name = "national_id")
    private String nationalId;

    @Size(max = 255, message = "Occupation must be less than 255 characters")
    @Column(name = "occupation")
    private String occupation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}