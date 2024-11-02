package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.annotation.auth.CurrentAccount;
import com.dut.pbl6_server.annotation.auth.PreAuthorizeUser;
import com.dut.pbl6_server.dto.request.ChangePasswordRequest;
import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.request.RefreshTokenRequest;
import com.dut.pbl6_server.dto.request.RegisterRequest;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("AuthController")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest, false);
    }

    @PostMapping("/refresh-token")
    public Object refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest, false);
    }

    @PostMapping("/revoke-token")
    @PreAuthorizeUser
    public Object revokeToken(@CurrentAccount Account account) {
        authService.revokeToken(account, false);
        return null;
    }

    @PostMapping("/register")
    public Object register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/password/change")
    @PreAuthorizeUser
    public Object changePassword(@CurrentAccount Account account, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(account, request);
        return null;
    }
}
