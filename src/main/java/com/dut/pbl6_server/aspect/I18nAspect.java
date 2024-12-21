package com.dut.pbl6_server.aspect;

import com.dut.pbl6_server.common.util.I18nUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class I18nAspect {
    @Pointcut("execution(* com.dut.pbl6_server.controller..*.*(..))")
    public void controllers() {
    }

    @Pointcut("within(com.dut.pbl6_server.common.exception.GlobalExceptionHandler))")
    public void globalExceptionHandler() {
    }

    // Set language before entering controller
    @Before("controllers()")
    public void setLanguageBefore(JoinPoint joinPoint) {
        if (!I18nUtils.notSetLanguage()) return;

        // Get request from RequestContextHolder
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();

            // Get 'lang' parameter from request
            String lang = request.getParameter("lang");

            I18nUtils.setLanguage(lang);
        }
    }


    // Reset current language after returning from controller
    @AfterReturning("controllers()")
    public void resetCurrentLanguage() {
        I18nUtils.clear();
    }

    // Reset current language after returning from global exception handler
    @After("globalExceptionHandler()")
    public void resetCurrentLanguageGlobalExceptionHandler() {
        I18nUtils.clear();
    }
}
