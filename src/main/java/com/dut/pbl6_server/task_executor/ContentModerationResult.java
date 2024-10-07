package com.dut.pbl6_server.task_executor;

import com.dut.pbl6_server.common.model.AbstractResponse;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentModerationResult {
    private AbstractResponse response;
    private boolean isTextModeratedDone;
    private boolean isImageModeratedDone;
}
