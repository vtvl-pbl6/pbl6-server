package com.dut.pbl6_server.config.auth;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.NotFoundObjectException;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserDetailConfig {
    private final AccountsRepository repo;

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            if (email != null && !email.isBlank()) {
                return repo.findByEmail(email)
                    .orElseThrow(() -> new NotFoundObjectException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
            }
            throw new NotFoundObjectException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
