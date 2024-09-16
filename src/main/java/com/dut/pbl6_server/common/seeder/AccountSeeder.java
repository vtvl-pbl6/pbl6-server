package com.dut.pbl6_server.common.seeder;

import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
@Slf4j
public class AccountSeeder implements CommandLineRunner {
    private final AccountsRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        final String passwordHash = passwordEncoder.encode("123456Aa");

        // User account
        var userEmail = "user@gmail.com";
        if (!repository.existsByEmail(userEmail)) {
            var userAccount = Account.builder()
                .email(userEmail)
                .password(passwordHash)
                .firstName("Vinh")
                .lastName("Pham")
                .role(AccountRole.USER)
                .displayName("vinhthanh.73")
                .build();
            try {
                repository.save(userAccount);
            } catch (Exception e) {
                log.error("Error seeding user account: {}", e.getMessage());
            }
        }

        // Admin account
        var adminEmail = "admin@gmail.com";
        if (!repository.existsByEmail(adminEmail)) {
            var adminAccount = Account.builder()
                .email(adminEmail)
                .password(passwordHash)
                .firstName("Admin")
                .lastName("PBL6")
                .role(AccountRole.ADMIN)
                .displayName("admin.pbl6")
                .build();
            try {
                repository.save(adminAccount);
            } catch (Exception e) {
                log.error("Error seeding admin account: {}", e.getMessage());
            }
        }
    }
}
