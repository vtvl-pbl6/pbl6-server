package com.dut.pbl6_server.common.util;

@FunctionalInterface
public interface VoidCallBack<T> {
    void call(T data);
}
