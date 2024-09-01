package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest, false);
    }

    @PostMapping("/init/account")
    public Object initAccount() {
        authService.initDefaultAccount();
        return null;
    }

    @GetMapping("/test")
    public Object test() {
        return "AuthController";
    }
}
