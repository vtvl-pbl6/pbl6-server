package com.dut.pbl6_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SpringAsyncConfig {

    @Bean(name = "contentModerationTaskExecutor")
    public TaskExecutor contentModerationTaskExecutor() {
        int poolSize = calculateThreadPoolSize(false, 0.7f, 0.8f);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("ContentModerationAsyncExecutor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);  // Ensures tasks complete on shutdown
        executor.setAwaitTerminationSeconds(900); // 15 minutes
        executor.initialize();
        return executor;
    }

    /**
     * Calculates the appropriate number of threads for a thread pool based on the nature of the tasks and desired CPU utilization.
     * <p>
     * This method helps to determine the optimal number of threads to be used in a thread pool depending on whether the tasks are
     * <b>CPU-bound</b> or <b>IO-bound</b>, the target CPU utilization, and the blocking coefficient (a measure of how much time threads spend
     * waiting for resources).
     * <p>
     * The formula used to determine the pool size is based on the following:
     * - If the tasks are CPU-bound, the number of threads should ideally match the number of available CPU cores to avoid unnecessary context switching.
     * - If the tasks are IO-bound (e.g., involving network calls or database access), the number of threads will depend on the <i>target CPU utilization</i> and the <i>blocking coefficient</i>.
     * <p>
     * Formula:
     * ThreadPoolSize = Number of Available Cores * Target CPU utilization * (1 + Blocking Coefficient)
     *
     * @param isCPUBoundTasks      A boolean flag indicating whether the tasks are CPU-bound (true) or IO-bound (false).
     *                             CPU-bound tasks perform heavy computation, while IO-bound tasks involve waiting for external resources.
     * @param targetCPUUtilization A float representing the target CPU utilization, typically between 0 and 1.
     *                             For instance, 0.75 means you aim to use 75% of the CPU's capacity.
     * @param blockingCoefficient  A float representing the percentage of time a thread spends waiting for external resources (e.g., IO, network).
     *                             This value ranges from 0 (no waiting, fully CPU-bound) to close to 1 (mostly waiting for IO).
     * @return The optimal number of threads to be allocated for the thread pool.
     */
    public int calculateThreadPoolSize(boolean isCPUBoundTasks, float targetCPUUtilization, float blockingCoefficient) {
        int availableCores = Runtime.getRuntime().availableProcessors();

        // Check valid parameters
        if ((targetCPUUtilization < 0 || targetCPUUtilization > 1) || (blockingCoefficient < 0 || blockingCoefficient > 1)) {
            throw new IllegalArgumentException("TargetCPUUtilization or blockingCoefficient parameter is invalid!");
        }

        int threadPoolSize = isCPUBoundTasks ? availableCores - 1 : (int) (availableCores * targetCPUUtilization * (1 + blockingCoefficient));

        return threadPoolSize > 0 ? threadPoolSize : 1; // Ensures a minimum pool size of 1
    }
}
