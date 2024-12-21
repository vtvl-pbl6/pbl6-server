package com.dut.pbl6_server.dto.request;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.annotation.validation.PasswordValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class ChangePasswordRequest {
    @PasswordValidation
    private String oldPassword;

    @PasswordValidation
    private String newPassword;

    @PasswordValidation
    private String confirmPassword;
}
