package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("request")
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormTemplateRepository formTemplateRepository;

    @Override
    public FormTemplate saveTemplate(FormTemplate template) {
        formTemplateRepository.save(template);
        return template;
    }

    @Override
    public FormTemplate copyTemplate(FormTemplate template) {
        return null;
    }

    @Override
    public FormTemplate getTemplate(String key) {
        return null;
    }

    @Override
    public Page<FormTemplate> getTemplates(int page, int size, String sortBy) {
        return getTemplates(page, size, sortBy);
    }

    @Override
    public Page<FormTemplate> getTemplates(int page, int size, String sortBy, String owner) {
        Sort sort = new Sort(Sort.Direction.ASC, sortBy);
        Pageable pageable = new PageRequest(page, size, sort);
        List<Selection> selections = new ArrayList<Selection>();

        if (owner != null) {
            selections.add(new Selection("id", Selection.Operator.EQUAL, owner));
        }

        return formTemplateRepository.findAllBy(pageable, selections);
    }
}
