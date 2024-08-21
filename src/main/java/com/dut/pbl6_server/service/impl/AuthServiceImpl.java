package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.ForbiddenException;
import com.dut.pbl6_server.config.auth.JwtUtils;
import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.respone.CredentialResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.RefreshToken;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.RefreshTokensRepository;
import com.dut.pbl6_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final RefreshTokensRepository refreshTokensRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AccountsRepository accountsRepository;


    public CredentialResponse login(LoginRequest loginRequest, boolean isAdmin) {
        try {
            Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Account account = (Account) authentication.getPrincipal();
            // Check if the user is an admin
            if (isAdmin && account.getRole() != AccountRole.ADMIN) {
                throw new ForbiddenException(ErrorMessageConstants.FORBIDDEN);
            }
            CredentialResponse response = jwtUtils.generateToken(account.getId());
            refreshTokensRepository.save(RefreshToken.builder().account(account).token(response.getRefreshToken()).build());
            return response;
        } catch (BadCredentialsException ex) {
            throw new BadRequestException(ErrorMessageConstants.INCORRECT_EMAIL_OR_PASSWORD);
        } catch (InternalAuthenticationServiceException ex) {
            throw new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
        } catch (DisabledException ex) {
            throw new ForbiddenException(ErrorMessageConstants.ACCOUNT_IS_DISABLED);
        }
    }

    @Override
    public void initDefaultAccount() {
        accountsRepository.save(Account.builder()
            .email("test@gmail.com")
            .password(passwordEncoder.encode("123456Aa"))
            .role(AccountRole.ADMIN)
            .build());
    }
}
