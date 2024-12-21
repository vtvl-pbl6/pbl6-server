package com.dut.pbl6_server.dto.request;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.annotation.validation.NotBlankStringValidation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class NotificationRequest {
    @NotNull
    private Long receiverId;
    
    @NotBlankStringValidation
    private String content;
}
