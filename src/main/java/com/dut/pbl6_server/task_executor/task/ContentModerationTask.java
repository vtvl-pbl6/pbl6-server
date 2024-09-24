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
public class ContentModerationTask extends BaseTask<AbstractResponse> {
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
        VoidCallBack<AbstractResponse> onDone
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
        // Handle the text
        if (CommonUtils.String.isNotEmptyOrNull(text)) {
            try {
                // Prepare the request body
                var requestBody = RequestBody.create(
                    Objects.requireNonNull(CommonUtils.Json.encode(Map.of(
                        "text", text,
                        "key", accessKey
                    ))),
                    MediaType.parse("application/json"));

                // Send the request to the content moderation server
                var execute = httpClient.newCall(httpClientConfig.getRequest(
                    getUrl(serverUrl, true),
                    HttpMethod.POST,
                    requestBody,
                    null)
                ).execute();

                String responseBody = execute.body() != null ? execute.body().string() : null;
                AbstractResponse abstractResponse = CommonUtils.Json.decode(responseBody, AbstractResponse.class);
                onDone.call(abstractResponse); // Notify the caller
            } catch (Exception e) {
                log.error("Error while processing text: " + e.getMessage());
            }
        }

        // Handle the files
        if (CommonUtils.List.isNotEmptyOrNull(files)) {
            try {
                // Prepare the request body
                var requestBody = RequestBody.create(
                    Objects.requireNonNull(CommonUtils.Json.encode(Map.of(
                        "files", files.stream().map(File::getUrl).toList(),
                        "key", accessKey
                    ))),
                    MediaType.parse("application/json"));


                // Send the request to the content moderation server
                var execute = httpClient.newCall(httpClientConfig.getRequest(
                    getUrl(serverUrl, false),
                    HttpMethod.POST,
                    requestBody,
                    null)
                ).execute();

                String responseBody = execute.body() != null ? execute.body().string() : null;
                AbstractResponse abstractResponse = CommonUtils.Json.decode(responseBody, AbstractResponse.class);
                onDone.call(abstractResponse); // Notify the caller
            } catch (Exception e) {
                log.error("Error while processing file: " + e.getMessage());
            }
        }
    }

    // TODO: Implement url for image
    private static String getUrl(String serverUrl, boolean isText) {
        return serverUrl + (isText ? "/hate-speech-text-span" : "");
    }
}
