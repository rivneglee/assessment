package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    protected EntityService<Assessment> getService() {
        return getApplicationContext().getBean(AssessmentService.class);
    }

    @RequestMapping(path="/activated/form/{id}",
            method={RequestMethod.GET})
    public ViewJSONWrapper getForm(@PathVariable String id) {
        Form form = ((AssessmentService)getService()).findForm(id);
        List<Long> ministries = (List<Long>)getUserProfile().get("ministries");
        if (ministries != null && !ministries.isEmpty()) {
            boolean asOwner = false;
            for (Long ministry : ministries) {
                if (ministry.toString().equalsIgnoreCase(form.getId())) {
                    asOwner = true;
                    break;
                }
            }
            if (!asOwner) {
                form = null;
            }
        }

        FormTemplateService templateService = getApplicationContext().getBean(FormTemplateService.class);

        Map<String, Object> results = new HashMap();
        if (form != null) {
            results.put("form", form);
            results.put("template", templateService.get(form.getAssessment().getTemplateGuid()));
        }
        return new ViewJSONWrapper(results);
    }

    @RequestMapping(path="/activated/{id}/specimen/guid/{code}",
            method={RequestMethod.GET})
    public ViewJSONWrapper getSpecimenGuid(@PathVariable String id, @PathVariable String code) {
        Specimen specimen = ((AssessmentService)getService()).findSpecimen(id, code);
        return new ViewJSONWrapper(specimen == null ? "":specimen.getGuid());
    }

    @RequestMapping(path="/activated/form/{id}",
            method={RequestMethod.PUT})
    public ViewJSONWrapper saveForm(@PathVariable String id, @RequestBody List<ActualValue> values) {
        Form form = ((AssessmentService)getService()).updateFormActualValues(id, values);
        return new ViewJSONWrapper(form);
    }

    @RequestMapping(path="/activated/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper listActivated(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                @RequestParam(value = "size", defaultValue = "4") Integer size,
                                @RequestParam(value = "sort", defaultValue = "id") String sort,
                                @RequestParam(value = "filterField", defaultValue = "") String field,
                                @RequestParam(value = "filterValue", defaultValue = "") String value ) {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, "A"));

        List<Long> ministries = (List<Long>)getUserProfile().get("ministries");
        if (ministries != null && ministries.size() > 0) {
            selections.add(new Selection("participants." + ministries.get(0), Selection.Operator.EXSITS, true));
            return new ViewJSONWrapper(getService().list(page, size , sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }

    @RequestMapping(path="/closed/form/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper listClosed(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "4") Integer size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "filterField", defaultValue = "") String field,
                                         @RequestParam(value = "filterValue", defaultValue = "") String value ) {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, "C"));
        List<Long> ministries = (List<Long>)getUserProfile().get("ministries");
        if (ministries != null && ministries.size() > 0) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, ministries.get(0)));
        }
        return new ViewJSONWrapper(getApplicationContext().getBean(FormService.class).list(page, size , sort, selections));
    }
}
