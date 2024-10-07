package com.dut.pbl6_server.config.httpclient;

import com.dut.pbl6_server.common.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {
    @Value("${application.api-key.header-name}")
    private String apiKeyHeader;
    @Value("${application.api-key.header-value}")
    private String apiKeyValue;

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.HOURS)
            .readTimeout(2, TimeUnit.HOURS)
            .writeTimeout(2, TimeUnit.HOURS)
            .build();
    }

    public Request getRequest(String url, HttpMethod method, RequestBody body, String token) {
        Request.Builder request = null;
        switch (method) {
            case GET:
                request = new Request.Builder()
                    .url(url)
                    .get();
            case POST:
            case PUT:
            case DELETE:
            case PATCH:
                request = new Request.Builder()
                    .url(url)
                    .method(method.getValue(), body)
                    .addHeader("Content-Type", "application/json");
            default:
                break;
        }
        if (request != null) {
            // Add API key to request header
            request.addHeader(apiKeyHeader, apiKeyValue);

            // Add token to request header
            if (CommonUtils.String.isNotEmptyOrNull(token))
                request.addHeader("Authorization", "Bearer " + token);

            return request.build();
        }
        return null;
    }

    public Request getFormDataRequest(String url, HttpMethod method, RequestBody body, String token) {
        Request.Builder request = null;
        switch (method) {
            case GET:
                request = new Request.Builder()
                    .url(url)
                    .get();
            case POST:
            case PUT:
            case DELETE:
            case PATCH:
                request = new Request.Builder()
                    .url(url)
                    .method(method.getValue(), body)
                    .addHeader("Content-Type", "multipart/form-data");
            default:
                break;
        }
        if (request != null) {
            // Add API key to request header
            request.addHeader(apiKeyHeader, apiKeyValue);

            // Add token to request header
            if (CommonUtils.String.isNotEmptyOrNull(token))
                request.addHeader("Authorization", "Bearer " + token);

            return request.build();
        }
        return null;
    }
}
