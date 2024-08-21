package com.dut.pbl6_server.service;

import com.dut.pbl6_server.dto.request.LoginRequest;
import com.dut.pbl6_server.dto.respone.CredentialResponse;

public interface AuthService {
    CredentialResponse login(LoginRequest loginRequest, boolean isAdmin);

    void initDefaultAccount();
}
