package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.CertificationModel;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.assessment.services.AssessmentService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormService;
import com.stardust.easyassess.core.exception.MinistryOnlyException;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;

import jxl.write.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    @RequestMapping(method = {RequestMethod.POST})
    public ViewJSONWrapper add(@RequestBody Assessment model) throws Exception {
        if (preAdd(model)) {
            ((AssessmentService) getService()).createAssessment(model);
            return postAdd(getService().save(model));
        } else {
            return createEmptyResult();
        }
    }

    @Override
    protected ViewJSONWrapper postList(Page<Assessment> page) throws Exception {
        page.getContent().stream().forEach(assessment -> assessment.setForms(null));
        return super.postList(page);
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

    @RequestMapping(path = "/{id}/group/{group}/specimen/guid/{code}",
            method = {RequestMethod.GET})
    public ViewJSONWrapper getSpecimenGuid(@PathVariable String id, @PathVariable String group, @PathVariable String code) {
        Specimen specimen = ((AssessmentService) getService()).findSpecimen(id, group, code);
        return new ViewJSONWrapper(specimen == null ? "" : specimen.getGuid());
    }

    @RequestMapping(path = "/finalize/{id}",
            method = {RequestMethod.POST})
    public ViewJSONWrapper finalize(@PathVariable String id) {
        Assessment result = ((AssessmentService) getService()).finalizeAssessment(id);
        return new ViewJSONWrapper(result);
    }

    @RequestMapping(path = "/mine/activated/list",
            method = {RequestMethod.GET})
    public ViewJSONWrapper listActivated(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "4") Integer size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "filterField", defaultValue = "") String field,
                                         @RequestParam(value = "filterValue", defaultValue = "") String value) throws MinistryOnlyException {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        selections.add(new Selection("status", Selection.Operator.EQUAL, "A"));
        Owner owner = getNullableOwner();
        if (owner != null) {
            selections.add(new Selection("participants." + owner.getId(), Selection.Operator.EXSITS, true));
            return new ViewJSONWrapper(getService().list(page, size, sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }

    @RequestMapping(path = "/excel/{id}",
            method = {RequestMethod.GET})
    public void export(@PathVariable String id, HttpServletResponse response) throws IOException, WriteException {
        Assessment assessment = getService().get(id);
        if (assessment.getId().equals(id)) {
            response.reset();
            response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(assessment.getName(), "UTF-8") + ".xls");
            response.setContentType("application/msexcel");
            ((AssessmentService) getService()).exportToExcel(assessment, response.getOutputStream());
        }
    }

    @RequestMapping(path = "/{id}/participant", method = RequestMethod.PUT)
    public ViewJSONWrapper addParticipant(@PathVariable String id, @RequestBody Map<String, String> participant) throws Exception {
        return new ViewJSONWrapper(((AssessmentService) getService()).addParticipant(id, participant.get("participant"), participant.get("participantName")));
    }

    @RequestMapping(path = "/{id}/participant/{participantId}", method = RequestMethod.DELETE)
    public ViewJSONWrapper removeParticipant(@PathVariable String id, @PathVariable String participantId) throws Exception {
        return new ViewJSONWrapper(((AssessmentService) getService()).removeParticipant(id, participantId));
    }

    @RequestMapping(path = "/{id}/reopen", method = RequestMethod.GET)
    public ViewJSONWrapper reopen(@PathVariable String id) throws Exception {
        return new ViewJSONWrapper(((AssessmentService) getService()).reopenAssessment(id));
    }

    @RequestMapping(path = "/{id}/forms", method = RequestMethod.GET)
    public ViewJSONWrapper getForms(@PathVariable String id) throws Exception {
        Assessment assessment = getService().get(id);
        for (Form form : assessment.getForms()) {
            form.getTotalScore();
            form.setCodes(null);
            form.setDetails(null);
            form.setSignatures(null);
            form.setValues(null);
            form.setSubmitDate(null);
        }

        return new ViewJSONWrapper(assessment);
    }

    @RequestMapping(path = "/certification",
            method = {RequestMethod.GET})
    public void previewCertification(HttpServletResponse response,
                                     @RequestParam(defaultValue = "请输入证书颁发者") String certIssuer,
                                     @RequestParam(defaultValue = "请输入证书标题") String certTitle,
                                     @RequestParam(defaultValue = "请输入证书子标题") String certSubTitle,
                                     @RequestParam(defaultValue = "请输入证书内容") String certContent,
                                     @RequestParam(defaultValue = "请输入证书备注标题") String certCommentLabel,
                                     @RequestParam(defaultValue = "请输入证书备注") String certCommentContent) throws IOException {
        CertificationModel model = new CertificationModel();
        model.setDate(new Date());
        model.setOwner("XXXXX");
        model.setCommentLabel(certCommentLabel);
        model.setCommentContent(certCommentContent);
        model.setIssuerLabel("颁发者");
        model.setIssuer(certIssuer);
        model.setTitle(certTitle);
        model.setSubTitle(certSubTitle);
        model.setContent(certContent);
        ((AssessmentService)getService()).generateAssessmentCertification(model, response.getOutputStream());
    }
}
