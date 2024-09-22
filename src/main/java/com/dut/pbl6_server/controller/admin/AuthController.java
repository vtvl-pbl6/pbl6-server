package com.dut.pbl6_server.controller.admin;

import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.request.RefreshTokenRequest;
import com.dut.pbl6_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("AdminAuthController")
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest, true);
    }

    @PostMapping("/refresh-token")
    public Object refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest, true);
    }
}
