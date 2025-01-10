package js.project.account_service.config;

import js.project.account_service.model.BankBranch;
import js.project.account_service.repository.BankBranchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration // only for dev
public class InitialBankBranchConfig {

    @Bean
    //@Profile("dev") // This bean will only be created in the "dev" profile
    CommandLineRunner createInitialAdminUser(BankBranchRepository bankBranchRepository) {
        return args -> {
            Optional<BankBranch> existingBranch = bankBranchRepository.findByBranchCode("1234");
            if (existingBranch.isEmpty()) {
                BankBranch branch = BankBranch.builder()
                        .branchCode("1234")
                        .branchName("Test")
                        .location("india")
                        .build();
                bankBranchRepository.save(branch);
                System.out.println("test bank branch created.");
            }
        };
    }
}