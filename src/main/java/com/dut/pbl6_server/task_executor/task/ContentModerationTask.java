package com.dut.pbl6_server.task_executor.task;

import com.dut.pbl6_server.common.model.AbstractResponse;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.VoidCallBack;
import com.dut.pbl6_server.config.httpclient.HttpClientConfig;
import com.dut.pbl6_server.config.httpclient.HttpMethod;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.task_executor.BaseTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
@Log4j2
public class ContentModerationTask extends BaseTask<ContentModerationResult> {
    // External dependencies
    private final HttpClientConfig httpClientConfig;
    private final String serverUrl;
    private final String accessKey;
    private OkHttpClient httpClient;

    // Task parameters
    private String text;
    private List<File> files;

    public ContentModerationTask(
        HttpClientConfig httpClientConfig,
        String serverUrl,
        String accessKey,
        String text,
        List<File> files,
        VoidCallBack<ContentModerationResult> onDone
    ) {
        super(onDone);
        this.httpClientConfig = httpClientConfig;
        this.serverUrl = serverUrl;
        this.accessKey = accessKey;
        this.httpClient = httpClientConfig.client();
        this.text = text;
        this.files = files;
    }

    @Override
    public void run() {
        boolean isTextModeratedDone = CommonUtils.String.isEmptyOrNull(text); // isTextModeratedDone = true if text is null or empty
        boolean isImageModeratedDone = CommonUtils.List.isEmptyOrNull(files); // isImageModeratedDone = true if files is null or empty

        // Handle the text
        if (CommonUtils.String.isNotEmptyOrNull(text)) {
            try {
                var url = getUrl(serverUrl, true);
                // Prepare the request body
                var requestBody = RequestBody.create(
                    Objects.requireNonNull(CommonUtils.Json.encode(Map.of(
                        "text", text,
                        "key", accessKey
                    ))),
                    MediaType.parse("application/json"));

                // Send the request to the content moderation server
                var execute = httpClient.newCall(httpClientConfig.getRequest(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    null)
                ).execute();

                String responseBody = execute.body() != null ? execute.body().string() : null;
                log.info(String.format("%s\n%s", url, responseBody)); // Log the response body
                AbstractResponse response = CommonUtils.Json.decode(responseBody, AbstractResponse.class);
                isTextModeratedDone = true;
                onDone.call(new ContentModerationResult(response, true, false)); // Notify the caller
            } catch (Exception e) {
                log.error("Error while processing text: " + e.getMessage());
                isTextModeratedDone = true;
            }
        }

        // Handle the files
        if (CommonUtils.List.isNotEmptyOrNull(files)) {
            try {
                var url = getUrl(serverUrl, false);
                // Prepare the request body
                var requestBody = RequestBody.create(
                    Objects.requireNonNull(CommonUtils.Json.encode(Map.of(
                        "img_urls", files.stream().map(File::getUrl).toList(),
                        "key", accessKey
                    ))),
                    MediaType.parse("application/json"));

                // Send the request to the content moderation server
                var execute = httpClient.newCall(httpClientConfig.getRequest(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    null)
                ).execute();

                String responseBody = execute.body() != null ? execute.body().string() : null;
                log.info(String.format("%s\n%s", url, responseBody)); // Log the response body
                AbstractResponse response = CommonUtils.Json.decode(responseBody, AbstractResponse.class);
                isImageModeratedDone = true;
                onDone.call(new ContentModerationResult(response, false, true)); // Notify the caller
            } catch (Exception e) {
                log.error("Error while processing file: " + e.getMessage());
                isImageModeratedDone = true;
            }
        }

        onDone.call(new ContentModerationResult(null, isTextModeratedDone, isImageModeratedDone));
    }

    private static String getUrl(String serverUrl, boolean isText) {
        return serverUrl + (isText ? "/hate-speech-text-span" : "/nsfw");
    }
}
