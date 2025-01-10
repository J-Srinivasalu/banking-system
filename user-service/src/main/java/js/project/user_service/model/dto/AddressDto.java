package js.project.user_service.model.dto;

import js.project.user_service.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private UUID addressId;
    private String street;
    private String apartmentSuite;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public AddressDto(Address address) {
        if (address != null) {
            this.addressId = address.getAddressId();
            this.street = address.getStreet();
            this.apartmentSuite = address.getApartmentSuite();
            this.city = address.getCity();
            this.state = address.getState();
            this.zipCode = address.getZipCode();
            this.country = address.getCountry();
        }
    }

}