package js.project.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import js.project.user_service.exception.UserNotFoundException;
import js.project.user_service.model.Address;
import js.project.user_service.model.User;
import js.project.user_service.model.dto.AddressDto;
import js.project.user_service.model.dto.UserCreatedEvent;
import js.project.user_service.model.request.PatchUserRequest;
import js.project.user_service.model.request.UpdateUserRequest;
import js.project.user_service.model.response.GeneralResponse;
import js.project.user_service.model.dto.UserDto;
import js.project.user_service.repository.UserRepository;
import js.project.user_service.validation.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RequestValidator requestValidator;

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        log.info("Fetching user with ID: {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserDto response = new UserDto(user);
            log.info("User found: {}", response);
            return response;
        } else {
            log.warn("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    @Transactional
    public void createUser(UserCreatedEvent event) {
        log.info("Handling user created event: {}", event);
        try {
            Optional<User> userOptional = userRepository.findById(event.getUserId());
            if (userOptional.isPresent()) {
                log.info("User already exists with id: {}", event.getUserId());
                return;
            }

            AddressDto addressDto = event.getAddress();
            Address address = Address.builder()
                    .street(addressDto.getStreet())
                    .apartmentSuite(addressDto.getApartmentSuite())
                    .city(addressDto.getCity())
                    .state(addressDto.getState())
                    .zipCode(addressDto.getZipCode())
                    .country(addressDto.getCountry())
                    .build();


            User user = User.builder()
                    .userId(event.getUserId())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .email(event.getEmail())
                    .phoneNumber(event.getPhoneNumber())
                    .dateOfBirth(event.getDateOfBirth())
                    .address(address)
                    .nationality(event.getNationality())
                    .nationalId(event.getNationalId())
                    .occupation(event.getOccupation())
                    .build();

            userRepository.save(user);
            log.info("User created successfully from kafka event");

        } catch (Exception e) {
            log.error("Error creating user from Kafka event: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public GeneralResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setNationality(request.getNationality());
            user.setNationalId(request.getNationalId());
            user.setOccupation(request.getOccupation());

            AddressDto requestAddress = request.getAddress();
            if (requestAddress != null) {
                Address userAddress = user.getAddress();
                if (userAddress == null) {
                    userAddress = new Address();
                    user.setAddress(userAddress);
                }
                userAddress.setStreet(requestAddress.getStreet());
                userAddress.setApartmentSuite(requestAddress.getApartmentSuite());
                userAddress.setCity(requestAddress.getCity());
                userAddress.setState(requestAddress.getState());
                userAddress.setZipCode(requestAddress.getZipCode());
                userAddress.setCountry(requestAddress.getCountry());
            } else if (user.getAddress() != null) {
                user.setAddress(null);
            }

            try {
                userRepository.save(user);
                log.info("User with ID {} updated successfully.", userId);
                return new GeneralResponse("User updated successfully");
            } catch (Exception e) {
                log.error("Error updating user with ID {}: {}", userId, e.getMessage(), e);
                return new GeneralResponse("Error updating user");
            }

        } else {
            log.warn("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    @Transactional
    public GeneralResponse patchUser(UUID userId, Map<String, Object> updates) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            PatchUserRequest request = mapToPatchUserRequest(updates);
            requestValidator.validatePatchRequest(userId, request);
            updateUserFromRequest(user, request);
            userRepository.save(user);
            return new GeneralResponse("User updated successfully");
        } else {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
    }

    private PatchUserRequest mapToPatchUserRequest(Map<String, Object> updates) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(updates, PatchUserRequest.class);
    }

    private void updateUserFromRequest(User user, PatchUserRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate date = LocalDate.parse(request.getDateOfBirth(), formatter);
                user.setDateOfBirth(date);
            } catch (DateTimeParseException e) {
                log.error("Invalid date format", e);
                throw new IllegalArgumentException("Invalid date format");
            }
        }
        if (request.getAddress() != null) {
            Address userAddress = user.getAddress() != null ? user.getAddress() : new Address();
            if (request.getAddress().getStreet() != null) {
                userAddress.setStreet(request.getAddress().getStreet());
            }
            if (request.getAddress().getApartmentSuite() != null) {
                userAddress.setApartmentSuite(request.getAddress().getApartmentSuite());
            }
            if (request.getAddress().getCity() != null) {
                userAddress.setCity(request.getAddress().getCity());
            }
            if (request.getAddress().getState() != null) {
                userAddress.setState(request.getAddress().getState());
            }
            if (request.getAddress().getZipCode() != null) {
                userAddress.setZipCode(request.getAddress().getZipCode());
            }
            if (request.getAddress().getCountry() != null) {
                userAddress.setCountry(request.getAddress().getCountry());
            }
            user.setAddress(userAddress);
        }
        if (request.getNationality() != null) {
            user.setNationality(request.getNationality());
        }
        if (request.getNationalId() != null) {
            user.setNationalId(request.getNationalId());
        }
        if (request.getOccupation() != null) {
            user.setOccupation(request.getOccupation());
        }
    }

    @Transactional
    public GeneralResponse deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            try {
                userRepository.delete(optionalUser.get());
                log.info("User with ID {} deleted successfully.", userId);
                return new GeneralResponse("User deleted successfully");
            } catch (Exception e) {
                log.error("Error deleting user with ID {}: {}", userId, e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.warn("User not found with ID: {}", userId);
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);

        Page<User> usersPage = userRepository.findAll(pageable);

        if (usersPage.isEmpty()) {
            log.info("No users found on this page.");
            return Page.empty(pageable);
        }

        Page<UserDto> userResponsesPage = usersPage.map(UserDto::new);

        log.info("Found {} users on page {} of {} (total {} users).",
                userResponsesPage.getNumberOfElements(),
                userResponsesPage.getNumber(),
                userResponsesPage.getTotalPages(),
                userResponsesPage.getTotalElements());

        return userResponsesPage;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String firstName, String lastName, String email, Pageable pageable) {
        log.info("Searching users with filters: firstName={}, lastName={}, email={}, pageable={}",
                firstName, lastName, email, pageable);

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }

            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> usersPage = userRepository.findAll(spec, pageable);

        log.info("Found {} users matching the search criteria on page {} of {} (total {} users).",
                usersPage.getNumberOfElements(),
                usersPage.getNumber(),
                usersPage.getTotalPages(),
                usersPage.getTotalElements());

        return usersPage.map(UserDto::new);
    }
}
