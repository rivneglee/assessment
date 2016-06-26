package com.stardust.easyassess.assessment.dao;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoTemplateFactory implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static MongoTemplate get() {
        return applicationContext.getBean(MongoTemplate.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MongoTemplateFactory.applicationContext = applicationContext;
    }

}