package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.AdditionalScore;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.FormData;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.exception.ESAppException;
import com.stardust.easyassess.core.exception.MinistryOnlyException;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import jxl.write.WriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping({"{domain}/assess/form"})
@EnableAutoConfiguration
public class FormController extends MaintenanceController<Form> {
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    protected EntityService<Form> getService() {
        return getApplicationContext().getBean(FormService.class);
    }

    @RequestMapping(path="/{id}/score",
            method={RequestMethod.POST})
    public ViewJSONWrapper updateScore(@PathVariable String id, @RequestBody AdditionalScore score) throws MinistryOnlyException {
        Form form = getService().get(id);
        form.setAdditionalScore(score.getScore());
        form.setAdditationScoreDesc(score.getDesc());
        getService().save(form);
        return new ViewJSONWrapper(form);
    }

    @RequestMapping(path="/submit/{id}",
            method={RequestMethod.PUT})
    public ViewJSONWrapper submit(@PathVariable String id, @RequestBody FormData data) throws MinistryOnlyException {
        Form form = getOwnerFormById(id);
        form.setValues(data.getValues());
        form.setCodes(data.getCodes());
        form.setDetails(data.getDetails());
        form.setSignatures(data.getSignatures());
        ((FormService)getService()).submit(form);
        return new ViewJSONWrapper(form);
    }

    @RequestMapping(path="/{id}/attachment",
            method={RequestMethod.POST})
    public ViewJSONWrapper uploadAttachment(@PathVariable String id, @RequestParam("attachment") MultipartFile file) throws MinistryOnlyException, IOException {
        if(!file.isEmpty()) {
            String fileType=file.getOriginalFilename();
            if (fileType.contains(".")) {
                String[] parts = fileType.split("\\.");
                fileType = parts[parts.length - 1];
            }
            String link = ((FormService)getService()).addAttachment(id, fileType, file.getInputStream());
            return new ViewJSONWrapper(link);
        }

        return new ViewJSONWrapper(null);
    }

    @RequestMapping(path="/reject/{id}",
            method={RequestMethod.PUT})
    public ViewJSONWrapper reject(@PathVariable String id) throws MinistryOnlyException {
        Form form = getService().get(id);
        if (form.getId().equals(id) && form.getStatus().equals("C")) {
            form.setStatus("S");
            getService().save(form);
        }
        return new ViewJSONWrapper(form);
    }

    @Override
    public ViewJSONWrapper get(@PathVariable String id) throws MinistryOnlyException {
        Form form = getService().get(id);

        FormTemplateService templateService = getApplicationContext().getBean(FormTemplateService.class);

        Map<String, Object> results = new HashMap();
        if (form != null) {
            results.put("form", form);
            results.put("template", templateService.get(form.getAssessment().getTemplateGuid()));
        }
        return new ViewJSONWrapper(results);
    }

    @RequestMapping(path="/closed/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper getClosedForms(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "4") Integer size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "filterField", defaultValue = "") String field,
                                         @RequestParam(value = "filterValue", defaultValue = "") String value ) throws MinistryOnlyException {
        return buildFormList("F", page, size, sort, field, value);
    }

    @RequestMapping(path="/activated/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper getActivatedForms(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                      @RequestParam(value = "size", defaultValue = "4") Integer size,
                                      @RequestParam(value = "sort", defaultValue = "id") String sort,
                                      @RequestParam(value = "filterField", defaultValue = "") String field,
                                      @RequestParam(value = "filterValue", defaultValue = "") String value ) throws MinistryOnlyException {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.NOT_EQUAL, "F"));
        if (getOwner() != null) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, getOwner().getId()));
        }
        return new ViewJSONWrapper(getService().list(page, size , sort, selections));
    }

    @RequestMapping(path="/excel/{id}",
            method={RequestMethod.GET})
    public void export(@PathVariable String id, HttpServletResponse response) throws IOException, WriteException {
        Form form = getService().get(id);
        if (form.getStatus().equals("F") || form.getStatus().equals("C")) {
            response.reset();
            response.setHeader("Content-disposition", "attachment;filename=" +  java.net.URLEncoder.encode(form.getOwnerName() + "_" + form.getAssessment().getName(), "UTF-8") + ".xls");
            response.setContentType("application/msexcel");
            ((FormService)getService()).exportToExcel(form, response.getOutputStream());
        }
    }

    private Form getOwnerFormById(String id) throws MinistryOnlyException {
        Form form = getService().get(id);
        Owner owner = getOwner();
        if (owner != null) {
            if (!owner.getId().equals(form.getOwner())) {
                form = null;
            }
        }
        return form;
    }

    private ViewJSONWrapper buildFormList(String status, Integer page, Integer size, String sort, String field, String value) throws MinistryOnlyException {
        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, status));
        if (getOwner() != null) {
            selections.add(new Selection("owner", Selection.Operator.EQUAL, getOwner().getId()));
        }
        return new ViewJSONWrapper(getService().list(page, size , sort, selections));
    }

    @RequestMapping(path="/owner",
            method={RequestMethod.PUT})
    public ViewJSONWrapper updateOwner(@RequestBody Owner owner) throws ESAppException, ParseException {
        ((FormService)getService()).updateOwnerName(owner);
        return new ViewJSONWrapper(owner);
    }

    @RequestMapping(path="/{id}/certification",
            method={RequestMethod.GET})
    public void generateCertification(@PathVariable String id, HttpServletResponse response) throws IOException, WriteException {
        ((FormService)getService()).generateAssessmentCertification(id, response.getOutputStream());
    }
}
