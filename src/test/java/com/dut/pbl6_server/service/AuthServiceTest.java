package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.exception.ForbiddenException;
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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class AuthServiceTest {
    /* Mock beans and dependencies */
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private AccountsRepository accountsRepository;
    @MockBean
    private RefreshTokensRepository refreshTokensRepository;
    @MockBean
    private RedisRepository redisRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private AccountMapper accountMapper;
    @MockBean
    private Authentication authentication;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;

    /* Test cases */
    @Test
    void login_ValidLoginRequest_ReturnsCorrectCredentialResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        CredentialResponse expectedResponse = new CredentialResponse("access_token", "refresh_token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build());
        when(jwtUtils.generateToken(anyLong())).thenReturn(expectedResponse);
        when(refreshTokensRepository.save(any())).thenReturn(new RefreshToken());

        // Act
        CredentialResponse result = authService.login(loginRequest, false);

        // Assert
        assertEquals(expectedResponse, result);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtils, times(1)).generateToken(anyLong());
        verify(refreshTokensRepository, times(1)).save(any());
    }

    @Test
    void login_InvalidCredentials_ThrowsBadRequestException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("invalid@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> authService.login(loginRequest, false));
        assertEquals("incorrect_email_or_password", thrown.getMessage());
    }

    @Test
    void login_UserIsNotAdmin_ThrowsForbiddenException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(account);
        when(jwtUtils.generateToken(anyLong())).thenReturn(new CredentialResponse("access_token", "refresh_token"));

        // Act & Assert
        ForbiddenException thrown = assertThrows(ForbiddenException.class, () -> authService.login(loginRequest, true));
        assertEquals("forbidden", thrown.getMessage());
    }

    @Test
    void refreshToken_ValidRefreshToken_ReturnsNewCredentialResponse() {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("valid_refresh_token");
        CredentialResponse expectedResponse = new CredentialResponse("new_access_token", "new_refresh_token");
        when(jwtUtils.refreshToken(anyString(), anyBoolean())).thenReturn(expectedResponse);

        // Act
        CredentialResponse result = authService.refreshToken(refreshTokenRequest, false);

        // Assert
        assertEquals(expectedResponse, result);
        verify(jwtUtils, times(1)).refreshToken(anyString(), anyBoolean());
    }

    @Test
    void deleteAllExpiredRefreshTokens_DeletesExpiredTokens() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setCreatedAt(java.sql.Timestamp.valueOf("2023-01-01 00:00:00"));
        when(refreshTokensRepository.findAllExpired(any())).thenReturn(List.of(expiredToken));

        // Act
        authService.deleteAllExpiredRefreshTokens();

        // Assert
        verify(refreshTokensRepository, times(1)).deleteAll(any());
    }

    @Test
    void revokeToken_validToken_Success() {
        // Arrange
        Account account = Account.builder()
            .id(1L)
            .email("test@example.com")
            .password("password123")
            .role(AccountRole.ADMIN)
            .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(account, "valid_access_token");
        SecurityContext mockSecurityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            when(mockSecurityContext.getAuthentication()).thenReturn(authentication);

            // Act
            authService.revokeToken(account, true);

            // Assert
            verify(redisRepository, times(1)).save(anyString(), anyString(), eq(authentication.getCredentials().toString()));
        }
    }

    @Test
    void register_ValidRequest_ReturnsAccountResponse() {
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder().email("test@example.com").password("password123").confirmPassword("password123").build();
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        AccountResponse expectedResponse = AccountResponse.builder().id(1L).email("test@example.com").role(AccountRole.USER).build();

        when(accountsRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountsRepository.existsByDisplayName(anyString())).thenReturn(false);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(expectedResponse);
        when(accountsRepository.save(any(Account.class))).thenReturn(account);

        // Act
        AccountResponse result = authService.register(registerRequest);

        // Assert
        assertEquals(expectedResponse, result);
        verify(accountsRepository, times(1)).save(any());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder().email("test@example.com").password("password123").confirmPassword("password123").build();
        when(accountsRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        assertEquals("email_already_exists", thrown.getMessage());
    }

    @Test
    void changePassword_ValidRequest_Success() {
        // Arrange
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "newpassword1234", "newpassword1234");

        when(passwordEncoder.matches(changePasswordRequest.getOldPassword(), account.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(changePasswordRequest.getNewPassword(), account.getPassword())).thenReturn(false);
        when(accountsRepository.save(any(Account.class))).thenReturn(account);

        // Act
        authService.changePassword(account, changePasswordRequest);

        // Assert
        verify(accountsRepository, times(1)).save(any(Account.class));
    }

    @Test
    void changePassword_IncorrectOldPassword_ThrowsBadRequestException() {
        // Arrange
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("wrongpassword", "newpassword123", "newpassword123");

        when(passwordEncoder.matches(anyString(), eq(account.getPassword()))).thenReturn(false);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> authService.changePassword(account, changePasswordRequest));
        assertEquals("incorrect_old_password", thrown.getMessage());
    }

    @Test
    void changePassword_NewPasswordSameAsOld_ThrowsBadRequestException() {
        // Arrange
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "password123", "password123");

        when(passwordEncoder.matches(anyString(), eq(account.getPassword()))).thenReturn(true);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> authService.changePassword(account, changePasswordRequest));
        assertEquals("new_password_must_be_different", thrown.getMessage());
    }

    @Test
    void changePassword_ConfirmPasswordNotMatching_ThrowsBadRequestException() {
        // Arrange
        Account account = Account.builder().id(1L).email("test@example.com").password("password123").role(AccountRole.USER).build();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "newpassword1234", "differentpassword");

        when(passwordEncoder.matches(changePasswordRequest.getOldPassword(), account.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(changePasswordRequest.getNewPassword(), account.getPassword())).thenReturn(false);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> authService.changePassword(account, changePasswordRequest));
        assertEquals("confirm_password_not_matching", thrown.getMessage());
    }
}
