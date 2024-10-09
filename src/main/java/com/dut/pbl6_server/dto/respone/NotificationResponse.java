package com.dut.pbl6_server.dto.respone;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.common.model.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonSnakeCaseNaming
public class NotificationResponse extends AbstractDTO {
    private Long senderId;
    private Long receiverId;
    private Long objectId;
    private String type;
    private String content;
    private boolean isRead;
}
