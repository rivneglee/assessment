package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.Form;

import java.util.List;


public interface FormService extends EntityService<Form> {
    Form submit(Form form, List<ActualValue> values);
}
