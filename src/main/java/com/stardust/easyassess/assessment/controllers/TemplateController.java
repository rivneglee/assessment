package com.stardust.easyassess.assessment.controllers;

import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.exception.ESAppException;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping({"{domain}/assess/template"})
@EnableAutoConfiguration
public class TemplateController extends MaintenanceController<FormTemplate> {
    @Override
    protected EntityService<FormTemplate> getService() {
        return applicationContext.getBean(FormTemplateService.class);
    }

    @Override
    protected boolean preAdd(FormTemplate model) throws Exception {
        Owner owner = getNullableOwner();
        if (owner != null) {
            model.setOwner(owner.getId());
        }
        return super.preAdd(model);
    }

    @Override
    protected boolean preUpdate(String id, FormTemplate model) throws Exception {
        Owner owner = getNullableOwner();
        if (owner != null) {
            model.setOwner(owner.getId());
        }
        return super.preUpdate(id, model);
    }

    @Override
    protected boolean preList(List<Selection> selections) throws ESAppException {
        Owner owner = getNullableOwner();
        if (owner != null && owner.getId() != null && !owner.getId().isEmpty()) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, owner.getId()));
            selections.add(new Selection("owner", Selection.Operator.IS_NULL, null, Selection.Operand.OR));
        }
        return true;
    }
}
