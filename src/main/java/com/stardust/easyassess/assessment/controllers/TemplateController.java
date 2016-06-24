package com.stardust.easyassess.assessment.controllers;

import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.assessment.services.FormService;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping({"{domain}/assess/template"})
@EnableAutoConfiguration
public class TemplateController extends ActionController {

    @ResponseBody
    @RequestMapping(method={RequestMethod.POST})
    public ViewJSONWrapper save(@RequestBody FormTemplate template) {
        FormService service = getApplicationContext().getBean(FormService.class);
        service.saveTemplate(template);
        return new ViewJSONWrapper(template);
    }

    @RequestMapping(path="/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper list(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                @RequestParam(value = "size", defaultValue = "4") Integer size,
                                @RequestParam(value = "sort", defaultValue = "id") String sort,
                                @RequestParam(value = "filterField", defaultValue = "") String field,
                                @RequestParam(value = "filterValue", defaultValue = "") String value ) {

        FormService service = applicationContext.getBean(FormService.class);
        return new ViewJSONWrapper(service.getTemplates(page, size, sort));
    }
}
