package com.dut.pbl6_server.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    @Before("controllerMethods()")
    public void logClientInfo(JoinPoint joinPoint) {
        // Get the HTTP request object
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Log request details
            String clientIp = request.getRemoteAddr();
            String method = request.getMethod();
            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString();
            String userAgent = request.getHeader("User-Agent");

            if (queryString != null) {
                logger.info("IP: {}, URI: {} - {}, Query String: {}, User-Agent: {}",
                    clientIp, method, requestUri, queryString, userAgent);
            } else {
                logger.info("IP: {}, URI: {} - {}, User-Agent: {}",
                    clientIp, method, requestUri, userAgent);
            }
        }
    }
}
