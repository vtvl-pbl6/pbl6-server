package com.dut.pbl6_server.scheduler;

import com.dut.pbl6_server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableAsync
public class DatabaseScheduler {
    private final AuthService authService;

    /**
     * Delete all expired refresh tokens every day at midnight
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredRefreshTokens() {
        try {
            authService.deleteAllExpiredRefreshTokens();
        } catch (Exception ignored) {
        }
    }
}
