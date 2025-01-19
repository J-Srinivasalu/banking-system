package js.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private UUID addressId;
    private String street;
    private String apartmentSuite;
    private String city;
    private String state;
    private String zipCode;
    private String country;

}