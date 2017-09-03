package com.stardust.easyassess.assessment.aspects;


import com.stardust.easyassess.core.context.ContextSession;
import com.stardust.easyassess.core.presentation.Message;
import com.stardust.easyassess.core.presentation.ResultCode;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Aspect
@Component
@Order(1)
public class AuthenticationAspect {

    @Autowired
    ApplicationContext applicationContext;

    public AuthenticationAspect() {

    }

    @Pointcut("execution(* com.stardust.easyassess.assessment.controllers.*Controller.*(..))")
    public void controllerRequest() {
    }

    @Before("controllerRequest()")
    public void doBefore(JoinPoint joinPoint) throws Throwable  {

    }

    @Around("controllerRequest()")
    public Object aroundControllerRequest(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        ContextSession session = applicationContext.getBean(ContextSession.class);
        if (session.get("userProfile") == null
                && !pjp.toString().contains("generateCertification")) {
            session.clear();
            clearCookie();
            result = new ViewJSONWrapper(new Message("503"), ResultCode.FAILED);
        } else {
            result = pjp.proceed();
        }
        return result;
    }

    @After("controllerRequest()")
    public void doAfter(JoinPoint joinPoint) throws Throwable  {

    }

    @AfterThrowing(pointcut = "controllerRequest()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Throwable {

    }

    private void clearCookie() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletResponse response = sra.getResponse();
        Cookie cookie = new Cookie("SESSION", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}