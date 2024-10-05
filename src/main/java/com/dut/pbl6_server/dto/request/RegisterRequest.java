package com.dut.pbl6_server.dto.request;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.annotation.validation.PasswordValidation;
import com.dut.pbl6_server.annotation.validation.UsernameValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @UsernameValidation
    private String username;

    @PasswordValidation
    private String password;

    private String confirmPassword;
}
