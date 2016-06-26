package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.form.FormTemplate;

public interface FormTemplateRepository extends DataRepository<FormTemplate, String> {
    default Class<FormTemplate> getEntityClass() {
        return FormTemplate.class;
    }
}
