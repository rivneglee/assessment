package com.stardust.easyassess.assessment.controllers;

import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping({"{domain}/assess/template"})
@EnableAutoConfiguration
public class TemplateController extends MaintenanceController<FormTemplate> {

    @ResponseBody
    @RequestMapping(method = {RequestMethod.POST})
    public ViewJSONWrapper save(@RequestBody FormTemplate template) {
        getService().save(template);
        return new ViewJSONWrapper(template);
    }

    @Override
    protected EntityService<FormTemplate> getService() {
        return applicationContext.getBean(FormTemplateService.class);
    }
}
