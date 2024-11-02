package com.dut.pbl6_server.task_executor.service;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.httpclient.HttpClientConfig;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.ThreadFile;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.json.HosResult;
import com.dut.pbl6_server.entity.json.Nsfw;
import com.dut.pbl6_server.repository.jpa.FilesRepository;
import com.dut.pbl6_server.repository.jpa.ThreadsRepository;
import com.dut.pbl6_server.service.NotificationService;
import com.dut.pbl6_server.task_executor.BaseTaskService;
import com.dut.pbl6_server.task_executor.task.ContentModerationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final NotificationService notificationService;
    private final FilesRepository filesRepository;

    @SuppressWarnings("unchecked")
    public void moderate(Thread thread) {
        submitTask(
            new ContentModerationTask(
                httpClientConfig,
                serverUrl,
                accessKey,
                thread.getContent(),
                CommonUtils.List.isNotEmptyOrNull(thread.getFiles())
                    ? thread.getFiles().stream().map(ThreadFile::getFile).toList()
                    : null,
                result -> {
                    if (result == null || result.getResponse() == null) return;
                    try {
                        var response = result.getResponse();
                        if (!response.getIsSuccess()) { // Log error if response is not success
                            response.getErrors().forEach(e -> log.error(e.toString()));
                            return;
                        }

                        // Handle text moderation response
                        if (result.isTextModeratedDone()) {
                            var textSpanDetectionResult = (Map<String, ?>) response.getData();
                            var hosSpans = (List<Map<String, ?>>) textSpanDetectionResult.get("hos_spans");
                            if (CommonUtils.List.isNotEmptyOrNull(hosSpans)) {
                                List<HosResult> hosResults = hosSpans.stream().map(HosResult::fromMap).toList();
                                // Save to database
                                if (CommonUtils.List.isNotEmptyOrNull(hosResults)) {
                                    thread.setHosResult(hosResults);
                                    threadsRepository.save(thread);
                                }
                            }
                        }

                        // Handle image moderation response
                        if (result.isImageModeratedDone()) {
                            var nsfwDetectionResult = (Map<String, ?>) response.getData();
                            var nsfw = (List<Map<String, ?>>) nsfwDetectionResult.get("nsfw");
                            List<Nsfw> detectionResult = nsfw.stream().map(Nsfw::fromMap).toList();
                            if (CommonUtils.List.isNotEmptyOrNull(detectionResult)) {
                                Map<String, Nsfw> resultMap = detectionResult.stream().collect(Collectors.toMap(Nsfw::getUrl, nsfw1 -> nsfw1));
                                // Save to database
                                if (CommonUtils.List.isNotEmptyOrNull(detectionResult)) {
                                    var threadFile = thread.getFiles().stream().map(ThreadFile::getFile).toList();
                                    var nsfwFile = threadFile.stream().peek(
                                        f -> {
                                            var nsfwResult = resultMap.get(f.getUrl());
                                            if (nsfwResult != null) f.setNsfwResult(nsfwResult);
                                        }
                                    ).filter(f -> f.getNsfwResult() != null).toList();
                                    if (CommonUtils.List.isNotEmptyOrNull(nsfwFile))
                                        filesRepository.saveAll(nsfwFile);
                                }
                            }
                        }

                        // Change thread status and send notification to client if both text and image moderation are done
                        if (result.isTextModeratedDone() && result.isImageModeratedDone()) {
                            // Change thread status
                            thread.setStatus(ThreadStatus.CREATE_DONE);
                            threadsRepository.save(thread);

                            // Send notification to client (via WebSocket)
                            notificationService.sendNotification(null, thread.getAuthor(), NotificationType.CREATE_THREAD_DONE, thread, false, false);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
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
