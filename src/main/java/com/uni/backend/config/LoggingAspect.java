package com.uni.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // it will run before any methods from service pack
    @Before("execution(* com.uni.backend.service.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("AOP Log: {} from class {} is running.",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName());
    }

    // it will run if a method from service pack throws an exception
    @AfterThrowing(pointcut = "execution(* com.uni.backend.service.*.*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("AOP Log: An exception was thrown from {} because of {}",
                joinPoint.getSignature().getName(),
                exception.getMessage());
    }
}
