package js.project.auth_service.config;

import js.project.auth_service.model.Role;
import js.project.auth_service.model.User;
import js.project.auth_service.repository.UserRepository;
import js.project.auth_service.security.UserDetailsServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration // only for dev
public class InitialAdminUserConfig {




    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository){
        return new UserDetailsServiceImpl(userRepository);
    }

    @Bean
    //@Profile("dev") // This bean will only be created in the "dev" profile
    CommandLineRunner createInitialAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existingAdmin = userRepository.findByEmail("admin@project.js");
            if (existingAdmin.isEmpty()) {
                User adminUser = User.builder()
                        .email("admin@project.js")
                        .password(passwordEncoder.encode("adminPassword"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(adminUser);
                System.out.println("Initial admin user created.");
            }
        };
    }
}