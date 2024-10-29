package com.dut.pbl6_server.dto.respone;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.common.model.AbstractDTO;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonSnakeCaseNaming
public class ThreadResponse extends AbstractDTO {
    private AccountResponse author;
    private ThreadResponse parentThread;
    private String content;
    private int reactionNum;
    private int sharedNum;
    private int commentNum;
    private boolean isPin;
    private ThreadStatus status;
    private Visibility visibility;
    private List<File> files;
    private List<AccountResponse> sharers;
    private List<AccountResponse> reactUsers;
    private List<ThreadResponse> comments;
}
