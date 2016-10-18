package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormService;
import com.stardust.easyassess.core.exception.MinistryOnlyException;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"{domain}/assess/assessment"})
@EnableAutoConfiguration
public class AssessmentController extends MaintenanceController<Assessment> {
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    @ResponseBody
    @RequestMapping(method={RequestMethod.POST})
    public ViewJSONWrapper add(@RequestBody Assessment model) throws Exception {
        if (preAdd(model)) {
            ((AssessmentService)getService()).createAssessment(model);
            return postAdd(getService().save(model));
        } else {
            return createEmptyResult();
        }
    }

    @Override
    protected boolean preList(List<Selection> selections) throws MinistryOnlyException {
        Owner owner = getNullableOwner();
        if (owner != null && owner.getId() != null && !owner.getId().isEmpty()) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, owner.getId()));
        }
        return true;
    }

    @Override
    protected boolean preAdd(Assessment model) throws Exception {
        Owner owner = getOwner();
        model.setOwner(owner.getId());
        model.setOwnerName(owner.getName());
        return super.preAdd(model);
    }

    @Override
    protected boolean preDelete(String id) throws Exception {
        FormService formService = applicationContext.getBean(FormService.class);
        Assessment assessment = getService().get(id);
        for (Form form : assessment.getForms()) {
            formService.remove(form.getId());
        }
        return true;
    }

    @Override
    protected boolean preUpdate(String id, Assessment model) throws Exception {
        Owner owner = getOwner();
        model.setOwner(owner.getId());
        model.setOwnerName(owner.getName());
        return super.preUpdate(id, model);
    }

    @Override
    protected EntityService<Assessment> getService() {
        return getApplicationContext().getBean(AssessmentService.class);
    }

    @RequestMapping(path="/{id}/group/{group}/specimen/guid/{code}",
            method={RequestMethod.GET})
    public ViewJSONWrapper getSpecimenGuid(@PathVariable String id, @PathVariable String group, @PathVariable String code) {
        Specimen specimen = ((AssessmentService)getService()).findSpecimen(id, group ,code);
        return new ViewJSONWrapper(specimen == null ? "":specimen.getGuid());
    }

    @RequestMapping(path="/finalize/{id}",
            method={RequestMethod.POST})
    public ViewJSONWrapper finalize(@PathVariable String id) {
        Assessment result = ((AssessmentService)getService()).finalizeAssessment(id);
        return new ViewJSONWrapper(result);
    }

    @RequestMapping(path="/mine/activated/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper listActivated(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                @RequestParam(value = "size", defaultValue = "4") Integer size,
                                @RequestParam(value = "sort", defaultValue = "id") String sort,
                                @RequestParam(value = "filterField", defaultValue = "") String field,
                                @RequestParam(value = "filterValue", defaultValue = "") String value ) throws MinistryOnlyException {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, "A"));
        Owner owner = getNullableOwner();
        if (owner != null) {
            selections.add(new Selection("participants." + owner.getId(), Selection.Operator.EXSITS, true));
            return new ViewJSONWrapper(getService().list(page, size , sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }
}
