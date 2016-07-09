package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@CrossOrigin("*")
@RestController
@RequestMapping({"{domain}/assess/assessment"})
@EnableAutoConfiguration
public class AssessmentController extends MaintenanceController<Assessment> {
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    @ResponseBody
    @RequestMapping(method={RequestMethod.POST})
    public ViewJSONWrapper add(@RequestBody Assessment model) {
        if (preAdd(model)) {
            ((AssessmentService)getService()).createAssessment(model);
            return postAdd(getService().save(model));
        } else {
            return createEmptyResult();
        }
    }

    @Override
    protected boolean preList(List<Selection> selections) {
        Owner owner = getOwner();
        if (owner != null && owner.getId() != null && !owner.getId().isEmpty()) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, owner.getId()));
        }
        return true;
    }

    @Override
    protected boolean preAdd(Assessment model) {
        Owner owner = getOwner();
        model.setOwner(owner.getId());
        model.setOwnerName(owner.getName());
        return super.preAdd(model);
    }

    @Override
    protected boolean preUpdate(String id, Assessment model) {
        Owner owner = getOwner();
        model.setOwner(owner.getId());
        model.setOwnerName(owner.getName());
        return super.preUpdate(id, model);
    }

    @Override
    protected EntityService<Assessment> getService() {
        return getApplicationContext().getBean(AssessmentService.class);
    }

    @RequestMapping(path="/{id}/specimen/guid/{code}",
            method={RequestMethod.GET})
    public ViewJSONWrapper getSpecimenGuid(@PathVariable String id, @PathVariable String code) {
        Specimen specimen = ((AssessmentService)getService()).findSpecimen(id, code);
        return new ViewJSONWrapper(specimen == null ? "":specimen.getGuid());
    }

    @RequestMapping(path="/mine/activated/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper listActivated(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                @RequestParam(value = "size", defaultValue = "4") Integer size,
                                @RequestParam(value = "sort", defaultValue = "id") String sort,
                                @RequestParam(value = "filterField", defaultValue = "") String field,
                                @RequestParam(value = "filterValue", defaultValue = "") String value ) {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, "A"));
        Owner owner = getOwner();
        if (owner != null) {
            selections.add(new Selection("participants." + owner.getId(), Selection.Operator.EXSITS, true));
            return new ViewJSONWrapper(getService().list(page, size , sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }
}
