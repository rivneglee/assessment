package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.models.form.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("request")
public class FormServiceImpl extends AbstractEntityService<Form> implements FormService {

    @Autowired
    FormRepository formRepository;

    @Override
    protected DataRepository getRepository() {
        return formRepository;
    }
}
