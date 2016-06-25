package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Service;

@Service
@Scope("request")
public class FormTemplateEntityServiceImpl extends AbstractEntityService<FormTemplate> implements FormTemplateService {
    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Override
    protected DataRepository getRepository() {
        return formTemplateRepository;
    }
}
