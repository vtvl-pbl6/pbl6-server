package com.dut.pbl6_server.task_executor;

import org.springframework.core.task.TaskExecutor;

public interface BaseTaskService<T extends BaseTask<?>> {
    TaskExecutor getTaskExecutor();

    default void submitTask(T task) {
        getTaskExecutor().execute(task);
    }
}
