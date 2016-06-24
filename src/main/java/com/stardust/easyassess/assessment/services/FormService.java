package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.models.form.FormTemplate;
import org.springframework.data.domain.Page;

public interface FormService {
    FormTemplate saveTemplate(FormTemplate template);

    FormTemplate copyTemplate(FormTemplate template);

    FormTemplate getTemplate(String key);

    Page<FormTemplate> getTemplates(int page, int size, String sortBy);

    Page<FormTemplate> getTemplates(int page, int size, String sortBy, String owner);
}
