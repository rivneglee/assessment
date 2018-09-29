package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.*;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private final String OSS_BUCKET_ENDPOINT = "http://assess-bucket.oss-cn-beijing.aliyuncs.com";

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
        ((AssessmentService)getService()).removeArticles(assessment);
        ((AssessmentService)getService()).removeAssets(assessment);
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
        Specimen specimen = ((AssessmentService) getService()).findSpecimen(id, group, code.replace("**slash**", "/"));
        return new ViewJSONWrapper(specimen == null ? "" : specimen.getGuid());
    }

    @RequestMapping(path = "/{id}/assets",
            method = {RequestMethod.GET})
    public ViewJSONWrapper getAssets(@PathVariable String id) {
        return new ViewJSONWrapper(((AssessmentService) getService()).getAssets(id));
    }

    @RequestMapping(path = "/{id}/assets",
            method = {RequestMethod.POST})
    public ViewJSONWrapper addAsset(@PathVariable String id, @RequestParam("asset") MultipartFile asset) throws IOException {
        if(!asset.isEmpty()) {
            return new ViewJSONWrapper(((AssessmentService) getService()).addAsset(id, asset.getOriginalFilename(), asset));
        }
        return new ViewJSONWrapper(null);
    }

    @RequestMapping(path = "/{id}/assets/{assetId}",
            method = {RequestMethod.DELETE})
    public ViewJSONWrapper removeAsset(@PathVariable String id, @PathVariable String assetId) {
        return new ViewJSONWrapper(((AssessmentService) getService()).removeAsset(id, assetId));
    }

    @RequestMapping(path = "/{id}/articles",
            method = {RequestMethod.GET})
    public ViewJSONWrapper getArticles(@PathVariable String id) {
        return new ViewJSONWrapper(((AssessmentService) getService()).getArticles(id));
    }

    @RequestMapping(path = "/{id}/articles/{articleId}",
            method = {RequestMethod.DELETE})
    public ViewJSONWrapper removeArticle(@PathVariable String id, @PathVariable String articleId) {
        return new ViewJSONWrapper(((AssessmentService) getService()).removeArticle(id, articleId));
    }

    @RequestMapping(path = "/{id}/articles",
            method = {RequestMethod.POST})
    public ViewJSONWrapper addArticle(@PathVariable String id, @RequestBody Article article) {
        return new ViewJSONWrapper(((AssessmentService) getService()).saveArticle(id, article));
    }

    @RequestMapping(path = "/{id}/articles/{articleId}",
            method = {RequestMethod.PUT})
    public ViewJSONWrapper updateArticle(@PathVariable String id, @PathVariable String articleId, @RequestBody Article article) {
        article.setId(articleId);
        return new ViewJSONWrapper(((AssessmentService) getService()).saveArticle(id, article));
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
            // form.setValues(null);
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
                                     @RequestParam(defaultValue = "请输入证书备注") String certCommentContent,
                                     @RequestParam(defaultValue = "") String certIssueDate) throws IOException, ParseException {
        CertificationModel model = new CertificationModel();
        if (certIssueDate.isEmpty()) {
            model.setDate(new Date());
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            model.setDate(simpleDateFormat.parse(certIssueDate));
        }
        model.setOwner("XXXXX");
        model.setCommentLabel(certCommentLabel);
        model.setCommentContent(certCommentContent);
        model.setIssuerLabel("颁发者");
        model.setIssuer(certIssuer);
        model.setTitle(certTitle);
        model.setSubTitle(certSubTitle);
        model.setContent(certContent);
        Owner owner = getNullableOwner();
        if (owner != null) {
            String signature = OSS_BUCKET_ENDPOINT + "/ministry-signature/signature_" + owner.getId() + ".png";
            model.setSignatureUrl(signature);
        }
        ((AssessmentService)getService()).generateAssessmentCertification(model, response.getOutputStream());
    }
}
