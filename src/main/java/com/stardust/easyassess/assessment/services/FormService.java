package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.form.Form;


public interface FormService extends EntityService<Form> {
    Form submit(Form form);
}
