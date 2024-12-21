package com.dut.pbl6_server.service;

import com.dut.pbl6_server.dto.request.ChangePasswordRequest;
import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.request.RefreshTokenRequest;
import com.dut.pbl6_server.dto.request.RegisterRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.dto.respone.CredentialResponse;
import com.dut.pbl6_server.entity.Account;

public interface AuthService {
    CredentialResponse login(LoginRequest loginRequest, boolean isAdmin);

    CredentialResponse refreshToken(RefreshTokenRequest refreshTokenRequest, boolean isAdmin);

    void deleteAllExpiredRefreshTokens();

    void revokeToken(Account account, boolean isAdmin);

    AccountResponse register(RegisterRequest registerRequest);

    void changePassword(Account account, ChangePasswordRequest request);
}
