package js.project.account_service.util;

import js.project.account_service.exception.BranchNotFoundException;
import js.project.account_service.model.BankBranch;
import js.project.account_service.repository.BankBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountNumberGenerator {

    private final BankBranchRepository bankBranchRepository;

    public String generateAccountNumber(String branchCode, long sequentialNumber) {
        log.info("Generating account number for branch {} and sequential number {}", branchCode, sequentialNumber);
        BankBranch branch = bankBranchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + branchCode));

        String formattedSerialNumber = String.format("%08d", sequentialNumber);
        String accountNumberWithoutCheckDigit = branch.getBankCode() + branch.getBranchCode() + formattedSerialNumber;
        int checkDigit = LuhnAlgorithm.generateCheckDigit(accountNumberWithoutCheckDigit);
        String accountNumber = accountNumberWithoutCheckDigit + checkDigit;
        log.info("Generated account number: {}", accountNumber);
        return accountNumber;
    }
}