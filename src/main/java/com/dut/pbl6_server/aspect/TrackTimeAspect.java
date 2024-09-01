package com.dut.pbl6_server.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class TrackTimeAspect {
    private final Logger logger = LoggerFactory.getLogger(TrackTimeAspect.class);

    @Around("@annotation(com.dut.pbl6_server.annotation.aspect.TrackTime)")
    public Object aroundTrackTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        var result = joinPoint.proceed();

        double timeTaken = (System.currentTimeMillis() - startTime) * 1.0 / 1000; // in seconds
        logger.info("Request: {} executed in {}s", joinPoint.getSignature(), timeTaken);
        return result;
    }
}
