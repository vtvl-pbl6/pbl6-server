package com.dut.pbl6_server.scheduler;

import com.dut.pbl6_server.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableAsync
@Log4j2
public class CloudinaryScheduler implements ApplicationListener<ApplicationReadyEvent> {
    private final CloudinaryService cloudinaryService;

    /**
     * Clear not used URLs after the application starts
     */
    @Async
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // Uncomment to run if needed
            // cloudinaryService.clearNotUsedUrl();
        } catch (Exception ignored) {
        }
    }
}
