package com.stardust.easyassess.assessment.controllers;

import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.exception.ESAppException;
import com.stardust.easyassess.core.exception.MinistryOnlyException;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
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
    protected boolean preDelete(String id) {
        FormTemplate template = getService().get(id);
        template.setStatus("D");
        getService().save(template);
        return true;
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
        }
        selections.add(new Selection("status", Selection.Operator.NOT_EQUAL, "D"));
        return true;
    }

    @RequestMapping(path="/shared/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper getSharedTemplates(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                             @RequestParam(value = "size", defaultValue = "4") Integer size,
                                             @RequestParam(value = "sort", defaultValue = "id") String sort,
                                             @RequestParam(value = "filterField", defaultValue = "") String field,
                                             @RequestParam(value = "filterValue", defaultValue = "") String value ) {
        List<Selection> selections = new ArrayList();
        Owner owner = getNullableOwner();
        if (owner != null && owner.getId() != null && !owner.getId().isEmpty()) {
            selections.add(new Selection("owner", Selection.Operator.NOT_EQUAL, owner.getId()));
            selections.add(new Selection("enableSharing", Selection.Operator.EQUAL, true));
        }
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.NOT_EQUAL, "D"));
        return new ViewJSONWrapper(getService().list(page, size , sort, selections));
    }
}
