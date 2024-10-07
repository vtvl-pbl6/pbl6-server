package com.dut.pbl6_server.dto.respone;

import com.dut.pbl6_server.annotation.json.JsonDateFormat;
import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.common.model.AbstractDTO;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.entity.enums.AccountGender;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.entity.enums.AccountStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonSnakeCaseNaming
public class AccountResponse extends AbstractDTO {
    private String email;
    private String firstName;
    private String lastName;
    private AccountStatus status;
    private AccountRole role;
    private String displayName;
    @JsonDateFormat
    private Timestamp birthday;
    private AccountGender gender;
    private String bio;
    private File avatarFile;
    private Visibility visibility;
    private String language;
    private List<AccountResponse> followers; // 'followers' are the users who follow this user
    private List<AccountResponse> followingUsers; // 'followingUsers' are the users whom this user is following
}
