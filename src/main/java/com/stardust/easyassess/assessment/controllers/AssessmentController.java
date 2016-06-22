package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;


@CrossOrigin("*")
@RestController
@RequestMapping({"{domain}/assess/assessment"})
@EnableAutoConfiguration
public class AssessmentController {
    @Autowired
    protected ApplicationContext applicationContext;

    @ResponseBody
    @RequestMapping(method={RequestMethod.POST})
    public ViewJSONWrapper create(@RequestBody Assessment assessment) {
        AssessmentService service = applicationContext.getBean(AssessmentService.class);
        service.create(assessment);
        return new ViewJSONWrapper(assessment);
    }
}
