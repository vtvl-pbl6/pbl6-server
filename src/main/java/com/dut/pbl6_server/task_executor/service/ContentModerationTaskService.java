package com.dut.pbl6_server.task_executor.service;

import com.dut.pbl6_server.common.model.AbstractResponse;
import com.dut.pbl6_server.common.util.VoidCallBack;
import com.dut.pbl6_server.config.httpclient.HttpClientConfig;
import com.dut.pbl6_server.config.websocket.WebSocketUtils;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.task_executor.BaseTaskService;
import com.dut.pbl6_server.task_executor.task.ContentModerationTask;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentModerationTaskService implements BaseTaskService<ContentModerationTask> {
    @Value("${application.content-moderation-server.url}")
    private String serverUrl;
    @Value("${application.content-moderation-server.access-key}")
    private String accessKey;
    private final HttpClientConfig httpClientConfig;
    private final TaskExecutor contentModerationTaskExecutor;
    private final ThreadsRepository threadsRepository;
    private final WebSocketUtils webSocketUtils;

    public void moderate(String text, List<File> files) {
        submitTask(new ContentModerationTask(httpClientConfig, serverUrl, accessKey, text, files, new VoidCallBack<AbstractResponse>() {
            @Override
            public void call(AbstractResponse data) {
                if (data == null) return;

                if (data.getIsSuccess()) {
                    // TODO: Handle the response
                } else {
                    data.getErrors().forEach(System.out::println);
                }
            }
        }));
    }

    @Override
    public TaskExecutor getTaskExecutor() {
        return contentModerationTaskExecutor;
    }
}
