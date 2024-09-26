package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.service.ThreadService;
import com.dut.pbl6_server.task_executor.service.ContentModerationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("PostService")
@RequiredArgsConstructor
public class ThreadServiceImpl implements ThreadService {
    private final ContentModerationTaskService contentModerationTaskService;
    private final ThreadsRepository threadsRepository;
    private final AccountsRepository accountsRepository;

    @Override
    public Object createThread(String text) {
        if (CommonUtils.String.isNotEmptyOrNull(text)) {
            var newThread = threadsRepository.save(
                Thread.builder()
                    .author(accountsRepository.findByEmail("user@gmail.com").orElse(null))
                    .content(text)
                    .build()
            );


            // Moderates the content of the text
            contentModerationTaskService.moderate(newThread, null);

            return "ok";
        }

        return null;
    }
}
