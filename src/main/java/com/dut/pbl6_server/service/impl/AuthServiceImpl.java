package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.constant.RedisCacheConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.ForbiddenException;
import com.dut.pbl6_server.common.exception.InternalServerException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.auth.JwtUtils;
import com.dut.pbl6_server.dto.request.ChangePasswordRequest;
import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.request.RefreshTokenRequest;
import com.dut.pbl6_server.dto.request.RegisterRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.dto.respone.CredentialResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.RefreshToken;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.mapper.AccountMapper;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.RefreshTokensRepository;
import com.dut.pbl6_server.repository.redis.RedisRepository;
import com.dut.pbl6_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final RefreshTokensRepository refreshTokensRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AccountsRepository accountsRepository;
    private final RedisRepository redisRepository;
    private final AccountMapper accountMapper;

    @Value("${application.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs;
    private static final int BATCH_SIZE = 5000;

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
    public CredentialResponse refreshToken(RefreshTokenRequest refreshTokenRequest, boolean isAdmin) {
        return jwtUtils.refreshToken(refreshTokenRequest.getRefreshToken(), isAdmin);
    }

    @Override
    public void deleteAllExpiredRefreshTokens() {
        var pageRequest = PageRequest.of(0, BATCH_SIZE);
        var expiredRefreshTokens = refreshTokensRepository.findAllExpired(pageRequest).stream().filter(e -> {
            var currentTime = CommonUtils.DateTime.getCurrentTimestamp();
            var duration = Duration.between(e.getCreatedAt().toInstant(), currentTime.toInstant());
            return duration.toMillis() >= refreshTokenExpirationMs;
        }).toList();
        if (CommonUtils.List.isNotEmptyOrNull(expiredRefreshTokens))
            refreshTokensRepository.deleteAll(expiredRefreshTokens);
    }

    @Override
    public void revokeToken(Account account, boolean isAdmin) {
        var accessToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();

        // Check permission
        if (account.getRole() != AccountRole.ADMIN && isAdmin)
            throw new ForbiddenException(ErrorMessageConstants.FORBIDDEN);

        // Save revoked access token and refresh token to Redis
        redisRepository.save(
            RedisCacheConstants.AUTH_KEY,
            RedisCacheConstants.REVOKE_ACCESS_TOKEN_HASH(account.getId(), true),
            accessToken
        );
    }

    @Override
    public AccountResponse register(RegisterRequest registerRequest) {
        if (accountsRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException(ErrorMessageConstants.EMAIL_ALREADY_EXISTS);
        }

        if (accountsRepository.existsByDisplayName(registerRequest.getUsername())) {
            throw new BadRequestException(ErrorMessageConstants.USERNAME_ALREADY_EXISTS);
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadRequestException(ErrorMessageConstants.CONFIRM_PASSWORD_NOT_MATCHING);
        }

        Account account = Account.builder()
            .displayName(registerRequest.getUsername())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .email(registerRequest.getEmail())
            .firstName("Default")
            .lastName("Name")
            .role(AccountRole.USER)
            .build();

        try {
            accountsRepository.save(account);
            return accountMapper.toResponse(account);
        } catch (Exception ex) {
            throw new InternalServerException(ErrorMessageConstants.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void changePassword(Account account, ChangePasswordRequest request) {
        // Check if the old password is correct
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword()))
            throw new BadRequestException(ErrorMessageConstants.INCORRECT_OLD_PASSWORD);

        // Check if the new password is the same as the old password
        if (passwordEncoder.matches(request.getNewPassword(), account.getPassword()))
            throw new BadRequestException(ErrorMessageConstants.NEW_PASSWORD_MUST_BE_DIFFERENT);

        // Check if the new password is the same as the confirmation password
        if (!request.getConfirmPassword().equals(request.getNewPassword()))
            throw new BadRequestException(ErrorMessageConstants.CONFIRM_PASSWORD_NOT_MATCHING);

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountsRepository.save(account);
    }
}
