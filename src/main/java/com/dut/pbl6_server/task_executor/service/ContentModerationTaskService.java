package com.dut.pbl6_server.task_executor.service;

import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.VoidCallBack;
import com.dut.pbl6_server.config.httpclient.HttpClientConfig;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.ThreadFile;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.json.HosResult;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.task_executor.BaseTaskService;
import com.dut.pbl6_server.task_executor.ContentModerationResult;
import com.dut.pbl6_server.task_executor.task.ContentModerationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationTaskService implements BaseTaskService<ContentModerationTask> {
    @Value("${application.content-moderation-server.url}")
    private String serverUrl;
    @Value("${application.content-moderation-server.access-key}")
    private String accessKey;
    private final HttpClientConfig httpClientConfig;
    private final TaskExecutor contentModerationTaskExecutor;
    private final ThreadsRepository threadsRepository;

    public void moderate(Thread thread, List<ThreadFile> threadFiles) {
        submitTask(
            new ContentModerationTask(
                httpClientConfig,
                serverUrl,
                accessKey,
                thread.getContent(),
                CommonUtils.List.isNotEmptyOrNull(threadFiles) ? threadFiles.stream().map(ThreadFile::getFile).toList() : null,
                new VoidCallBack<ContentModerationResult>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void call(ContentModerationResult result) {
                        if (result == null || result.getResponse() == null) return;
                        try {
                            var response = result.getResponse();
                            if (!response.getIsSuccess()) { // Log error if response is not success
                                response.getErrors().forEach(e -> log.error(e.toString()));
                                return;
                            }
                            var threadTmp = threadsRepository.findById(thread.getId()).orElseThrow(() -> new BadRequestException("Thread not found"));

                            // Handle text moderation response
                            if (result.isTextModeratedDone()) {
                                var textSpanDetectionResult = (Map<String, ?>) response.getData();
                                var hosSpans = (List<Map<String, ?>>) textSpanDetectionResult.get("hos_spans");
                                List<HosResult> hosResults = hosSpans.stream().map(HosResult::fromMap).toList();
                                // Save to database
                                if (CommonUtils.List.isNotEmptyOrNull(hosResults)) {
                                    threadTmp.setHosResult(hosResults);
                                    threadsRepository.save(thread);
                                }
                            }

                            // Handle image moderation response
                            if (result.isImageModeratedDone()) {
                                // TODO: Handle image moderation response
                            }

                            // Change thread status and send notification to client if both text and image moderation are done
                            if (result.isTextModeratedDone() && result.isImageModeratedDone()) {
                                // Change thread status
                                threadTmp.setStatus(ThreadStatus.CREATE_DONE);
                                threadsRepository.save(threadTmp);

                                // TODO: Send notification to client
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }
            )
        );
    }

    @Override
    public TaskExecutor getTaskExecutor() {
        return contentModerationTaskExecutor;
    }
}
