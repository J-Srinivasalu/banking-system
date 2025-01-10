package js.project.account_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "bank_branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankBranch {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, unique = true)
    private UUID id;

    // having second thoughts
//    @NotBlank(message = "Bank code is required")
//    @Size(min = 4, max = 4, message = "Bank code must be 4 characters")
//    @Column(name = "bank_code", nullable = false, length = 4)
//    private String bankCode;

    @NotBlank(message = "Branch code is required")
    @Size(min = 4, max = 4, message = "Branch code must be 4 characters")
    @Column(name = "branch_code", nullable = false, length = 4)
    private String branchCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "location")
    private String location;

}