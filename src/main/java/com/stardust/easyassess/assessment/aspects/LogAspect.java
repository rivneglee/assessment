package com.stardust.easyassess.assessment.aspects;

import com.stardust.easyassess.core.presentation.Message;
import com.stardust.easyassess.core.presentation.ResultCode;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(2)
public class LogAspect {

    private static Logger logger = Logger.getLogger(LogAspect.class);

    public LogAspect() {

    }

    @Pointcut("execution(* com.stardust.easyassess.assessment.controllers.*Controller.*(..))")
    public void controllerRequest() {}

    @Pointcut("execution(* com.stardust.easyassess.assessment.services.*Service.*(..))")
    public void executeService() {}

    @Pointcut("execution(* com.stardust.easyassess.assessment.dao.repositories.*Repository.*(..))")
    public void accessRepository() {}

    @Before("executeService() || accessRepository()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        StringBuilder params = new StringBuilder();
        Object[] arguments = joinPoint.getArgs();

        for (Object p : arguments) {
            params.append(p.toString()).append(",");
        }

        logger.info(joinPoint.getTarget().getClass().getName() + "::" + joinPoint.getSignature().getName() + "(" + params.toString() + ")");
    }

    @Around("controllerRequest()")
    public Object aroundControllerRequest(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        logger.info("Requesting with method: [" + method + "], uri: [" + uri + "], params: [" + queryString + "]");
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            logger.error(e.getMessage());
            result = new ViewJSONWrapper(new Message("Error:" + e.getMessage()), ResultCode.FAILED);e.printStackTrace();
        }
        logger.info("Requesting completed");
        return result;
    }

    @AfterReturning(pointcut="controllerRequest()", argNames = "joinPoint,retVal", returning="retVal")
    public void doAfterReturning(JoinPoint joinPoint, Object retVal) throws Throwable {

    }

    @AfterThrowing(pointcut = "executeService() || accessRepository()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Throwable {
        logger.error(e.getMessage());
    }
}