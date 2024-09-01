package com.dut.pbl6_server.aspect;

import com.dut.pbl6_server.common.model.AbstractResponse;
import com.dut.pbl6_server.common.model.DataWithPage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ResponseEntityAspect {

    @Pointcut("execution(* com.dut.pbl6_server.controller.*.*(..))")
    public void controller() {
    }

    @Around("controller()")
    public ResponseEntity<AbstractResponse> modifyResponseAfterController(ProceedingJoinPoint joinPoint) throws Throwable {
        var val = joinPoint.proceed();
        AbstractResponse response = null;

        if (val == null)
            response = AbstractResponse.successWithoutMetaAndData();
        else if (val instanceof DataWithPage<?>)
            response = AbstractResponse.success(((DataWithPage<?>) val).getData(), ((DataWithPage<?>) val).getPageInfo());
        else
            response = AbstractResponse.successWithoutMeta(val);

        return ResponseEntity.ok(response);
    }
}
