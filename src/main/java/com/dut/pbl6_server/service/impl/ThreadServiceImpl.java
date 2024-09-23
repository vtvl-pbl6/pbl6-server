package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.service.ThreadService;
import com.dut.pbl6_server.task_executor.service.ContentModerationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("PostService")
@RequiredArgsConstructor
public class ThreadServiceImpl implements ThreadService {
    private final ContentModerationTaskService contentModerationTaskService;

    @Override
    public Object createThread(String text) {
        if (CommonUtils.String.isNotEmptyOrNull(text)) {
            // Moderates the content of the text
            contentModerationTaskService.moderate(text, null);

            return "ok";
        }

        return null;
    }
}
