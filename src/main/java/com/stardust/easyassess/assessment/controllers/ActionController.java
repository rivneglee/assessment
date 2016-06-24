package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.core.context.ContextSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ActionController {
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    protected HttpServletRequest getRequest() {
        return request;
    }

    protected HttpServletResponse getResponse() {
        return response;
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected ContextSession getSession() {
        return applicationContext.getBean(ContextSession.class);
    }
}
