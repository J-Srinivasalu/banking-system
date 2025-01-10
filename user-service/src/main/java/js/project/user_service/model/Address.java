package js.project.user_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "user_service_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @UuidGenerator
    @Column(name = "address_id", columnDefinition = "UUID")
    private UUID addressId;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be less than 255 characters")
    @Column(name = "street", nullable = false)
    private String street;

    @Size(max = 255, message = "Apartment/Suite must be less than 255 characters")
    @Column(name = "apartment_suite")
    private String apartmentSuite;

    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City must be less than 255 characters")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 255, message = "State must be less than 255 characters")
    @Column(name = "state", nullable = false)
    private String state;

    @NotBlank(message = "Zip code is required")
    @Size(max = 20, message = "Zip code must be less than 20 characters") // Adjust max size as needed
    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @NotBlank(message = "Country is required")
    @Size(max = 255, message = "Country must be less than 255 characters")
    @Column(name = "country", nullable = false)
    private String country;
}