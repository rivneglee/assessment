package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;


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
}
