package com.dut.pbl6_server.dto.request;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.entity.enums.AccountGender;
import com.dut.pbl6_server.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private Timestamp birthday;
    private AccountGender gender;
    private String bio;
    private Visibility visibility;
    private String language;

    public boolean hasAllNullFields() {
        return firstName == null && lastName == null && birthday == null && gender == null && bio == null && visibility == null && language == null;
    }
}