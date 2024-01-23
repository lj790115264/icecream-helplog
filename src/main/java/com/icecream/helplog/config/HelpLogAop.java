package com.icecream.helplog.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import com.icecream.helplog.util.HelpLog;

/**
 * @author andre.lan
 */
@Aspect
@Component
@Slf4j
public class HelpLogAop {

    @Pointcut("@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void logPointcut() {

    }

    @Around(value = "logPointcut()")
    public Object handler(ProceedingJoinPoint jp) throws Throwable {

        HelpLog.remove();
        Object response = null;
        try {

            response = jp.proceed();
        } finally {
            HelpLog.remove();
        }

        return response;
    }

}
