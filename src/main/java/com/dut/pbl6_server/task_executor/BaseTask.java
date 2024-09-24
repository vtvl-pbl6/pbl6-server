package com.dut.pbl6_server.task_executor;

import com.dut.pbl6_server.common.util.VoidCallBack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseTask<T> implements Runnable {
    protected VoidCallBack<T> onDone;

    public BaseTask(VoidCallBack<T> onDone) {
        this.onDone = onDone;
    }

    public abstract void run();
}
