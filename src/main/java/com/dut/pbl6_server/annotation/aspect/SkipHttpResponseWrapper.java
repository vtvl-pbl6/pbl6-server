package com.dut.pbl6_server.annotation.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipHttpResponseWrapper {
    // This annotation is used to skip the response entity wrapper in the ResponseEntityAspect
}
