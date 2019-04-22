package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.common.OSSBucketAccessor;
import com.stardust.easyassess.assessment.dao.repositories.*;
import com.stardust.easyassess.assessment.models.*;
import com.stardust.easyassess.assessment.models.form.*;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Scope("request")
public class AssessmentEntityServiceImpl extends AbstractEntityService<Assessment> implements AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    FormRepository formRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleReaderRepository articleReaderRepository;

    @Autowired
    FormTemplateService formTemplateService;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    ApplicationContext applicationContext;

    @Value("${assess.cert.server}")
    String certServer;

    @Override
    @Transactional
    public void createAssessment(Assessment assessment) {
        assessment.setStatus("A");
        assessment.setId(UUID.randomUUID().toString());
        FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());

        FormTemplate clone = new FormTemplate(template);
        clone.setStatus("D");
        formTemplateService.save(clone);
        assessment.setTemplateGuid(clone.getId());

        List<Form> forms = new ArrayList<Form>();
        for (String participant : assessment.getParticipants().keySet()) {
            Form form = new Form();
            form.setOwner(participant);
            form.setAssessment(assessment);
            form.setStatus("A");
            form.setFormName(assessment.getName());
            form.setTotalScore(new Double(0));
            forms.add(form);
            assessment.getForms().add(form);
            formRepository.save(form);
        }
        assessmentRepository.save(assessment);
    }




    @Override
    public List<Assessment> findByParticipant(String participant) {
        return assessmentRepository.findByParticipant(Long.parseLong(participant));
    }

    @Override
    public Form addParticipant(String assessmentId, String participant, String participantName) {
        Assessment assessment = this.get(assessmentId);
        if (assessment.getStatus().equals("A") && !assessment.getParticipants().containsKey(participant)) {
            assessment.getParticipants().put(participant, participantName);
            Form form = new Form();
            form.setOwner(participant);
            form.setAssessment(assessment);
            form.setStatus("A");
            form.setFormName(assessment.getName());
            form.setTotalScore(new Double(0));
            assessment.getForms().add(form);
            formRepository.save(form);
            assessmentRepository.save(assessment);
            if (assessment.getArticles() != null) {
                assessment.getArticles().forEach(article -> articleReaderRepository.save(new ArticleReader(article, participant)));
            }
            return form;
        }

        return null;
    }

    @Override
    @Transactional
    public Form removeParticipant(String assessmentId, String participantId) {
        Assessment assessment = this.get(assessmentId);
        if (assessment.getStatus().equals("A") && assessment.getParticipants().containsKey(participantId)) {
            for (Form form : assessment.getForms()) {
                if (form.getOwner().equals(participantId)) {
                    assessment.getForms().remove(form);
                    assessment.getParticipants().remove(participantId);
                    formRepository.delete(form);
                    assessmentRepository.save(assessment);
                    if (assessment.getArticles() != null) {
                        assessment.getArticles().forEach(article -> articleReaderRepository.removeArticlesReaderByArticleIdAndReaderId(article.getId(), participantId));
                    }
                    return form;
                }
            }
        }

        return null;
    }

    @Override
    public Assessment reopenAssessment(String assessmentId) {
        Assessment assessment = this.get(assessmentId);
        if (assessment.getStatus().equals("F")) {
            for (Form form : assessment.getForms()) {
                if (form.getStatus().equals("F")) {
                    form.setStatus("C");
                }
                form.getValues().forEach(v -> v.setScore(0.0));
                formRepository.save(form);
            }
            assessment.setStatus("A");
            assessmentRepository.save(assessment);
        }

        return assessment;
    }

    @Override
    public Specimen findSpecimen(String assessmentId, String groupId, String specimenCode) {
        Assessment assessment = this.get(assessmentId);
        Set<String> specimenNumberSet = new HashSet();
        if (assessment != null) {
            for (String specimenNumber : assessment.getSpecimenCodes().keySet()) {
                List<String> codes = assessment.getSpecimenCodes().get(specimenNumber);
                if (specimenCode.contains("+")) {
                    for (String subCode : specimenCode.split("\\+")) {
                        if (codes.contains(subCode)) {
                            specimenNumberSet.add(specimenNumber);
                        }
                    }
                } else if (codes.contains(specimenCode)) {
                    specimenNumberSet.add(specimenNumber);
                }
            }

            if (specimenNumberSet.size() > 0) {
                FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());

                for (GroupSection groups : template.getGroups()) {
                    if (!groups.getGuid().equals(groupId)) continue;
                    for (Specimen specimen : groups.getSpecimens()) {
                        if (!specimen.getNumber().contains("+")
                                && specimenNumberSet.contains(specimen.getNumber())) {
                            return specimen;
                        }

                        if (specimen.getNumber().contains("+")) {
                            String [] numbers = specimen.getNumber().split("\\+");
                            if (numbers.length == specimenNumberSet.size()
                                    && specimenNumberSet.containsAll(Arrays.asList(numbers))) {
                                return specimen;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Assessment finalizeAssessment(String id) {
        Assessment assessment = assessmentRepository.findOne(id);
        if (assessment != null && assessment.getStatus().equals("A")) {
            this.finalizeAssessment(assessment);
        }

        return assessment;
    }

    @Transactional
    @Override
    public void finalizeAssessment(Assessment assessment) {
        FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());

        Map<String, GroupRow> rowMap = new HashMap();

        for (GroupSection group : template.getGroups()) {
            for (GroupRow row : group.getRows()) {
                rowMap.put(row.getGuid(), row);
            }
        }

        for (Form form : assessment.getForms()) {
            for (ActualValue av : form.getValues()) {
                GroupRow row = rowMap.get(av.getSubjectGuid());
                if (row != null) {
                    av.setScore(calculateScore(row.getOptionMap().get(av.getSpecimenGuid()), av));
                    formRepository.save(form);
                }
            }

            //if (form.getStatus().equals("C")) {
                form.setStatus("F");
                formRepository.save(form);
            //}

        }
        assessment.setStatus("F");
        assessmentRepository.save(assessment);
    }


    private String getStatusText(Form form) {
        switch (form.getStatus()) {
            case "A":
            case "S":
                return "未完成";
            case "C":
                return "已提交";
            case "F":
                return "已审核";
        }
        return "未知状态";
    }

    // to-do: refactor later
    @Override
    public void exportToExcel(Assessment assessment, OutputStream outputStream) throws IOException, WriteException {
        FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());
        WritableWorkbook workbook = Workbook.createWorkbook(outputStream);
        WritableFont boldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableCellFormat labelFormat = new WritableCellFormat(boldFont);
        labelFormat.setBorder(Border.NONE, BorderLineStyle.THIN);
        labelFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        labelFormat.setAlignment(Alignment.CENTRE);
        labelFormat.setWrap(false);

        // render summary
        WritableSheet summarySheet = workbook.createSheet("总览", 0);
        summarySheet.addCell(new Label(0, 2, "单位名称", labelFormat));
        summarySheet.addCell(new Label(1, 2, "状态", labelFormat));
        summarySheet.addCell(new Label(2, 2, "考评得分", labelFormat));
        summarySheet.addCell(new Label(4, 2, "附加分", labelFormat));
        summarySheet.addCell(new Label(6, 2, "总分", labelFormat));
        int rowIdx = 3;
        for (Form form : assessment.getForms()) {
            rowIdx++;
            summarySheet.addCell(new Label(0, rowIdx, form.getOwnerName(), labelFormat));
            summarySheet.addCell(new Label(1, rowIdx, getStatusText(form), labelFormat));
            summarySheet.addCell(new Label(2, rowIdx, form.getTotalScore().toString(), labelFormat));
            summarySheet.addCell(new Label(4, rowIdx, form.getAdditionalScore().toString(), labelFormat));
            summarySheet.addCell(new Label(6, rowIdx, new Double(form.getTotalScore() + form.getAdditionalScore()).toString(), labelFormat));
        }

        // render groups
        for (int i = 0; i < template.getGroups().size(); i++) {
            GroupSection group = template.getGroups().get(i);
            if (group.getName() == null || group.getRows().size() == 0) continue;
            WritableSheet sheet = workbook.createSheet(group.getName(), i + 1);
            // render rows
            for (int j = 0; j < group.getRows().size(); j++) {
                GroupRow row = group.getRows().get(j);
                int startIndex = (j * assessment.getForms().size()) + 1;
                sheet.addCell(new Label(0, startIndex, row.getItem().getSubject() + "-" + row.getItem().getUnit(), labelFormat));
                sheet.mergeCells(0, startIndex, 0, startIndex + assessment.getForms().size() - 1);
                for (int h = 0; h < assessment.getForms().size(); h++) {
                    Form form = assessment.getForms().get(h);
                    sheet.addCell(new Label(1, (j * assessment.getForms().size()) + h + 1, form.getOwnerName(), labelFormat));
                    // render values
                    for (int k = 0; k < group.getSpecimens().size(); k++) {
                        Specimen specimen = group.getSpecimens().get(k);
                        for (ActualValue value : form.getValues()) {
                            if (value.getSpecimenGuid().equals(specimen.getGuid())
                                    && value.getSubjectGuid().equals(row.getGuid())) {
                                WritableCell valueCell = new Label(k + 2, (j * assessment.getForms().size()) + h + 1, value.getValue(), labelFormat);
                                sheet.addCell(valueCell);
                                WritableCellFeatures cellFeatures = new WritableCellFeatures();
                                cellFeatures.setComment("盲样码:" + value.getSpecimenCode());
                                valueCell.setCellFeatures(cellFeatures);
                                break;
                            }
                        }
                    }
                    // render codes
                    for (int k = 0; k < group.getCodeGroups().size(); k++) {
                        CodeGroup codeGroup = group.getCodeGroups().get(k);
                        for (Code code : form.getCodes()) {
                            if (code.getCodeGroup().getGuid().equals(codeGroup.getGuid())
                                    && code.getSubjectGuid().equals(row.getGuid())) {
                                sheet.addCell(new Label(k + group.getSpecimens().size() + 3, (j * assessment.getForms().size()) + h + 1, code.getCodeName(), labelFormat));
                                break;
                            }
                        }
                    }

                    // render details
                    int columnIndex = group.getSpecimens().size() + 3 + group.getCodeGroups().size() + 1;
                    for (Map<String, String> detail : form.getDetails()) {
                        if (row.getGuid().equals(detail.get("subjectGuid"))) {
                            sheet.addCell(new Label(columnIndex, (j * assessment.getForms().size()) + h + 1, detail.get("batchNumber"), labelFormat));
                            sheet.addCell(new Label(columnIndex + 1, (j * assessment.getForms().size()) + h + 1, detail.get("expire"), labelFormat));
                        }
                    }
                }

            }

            // render specimen title
            for (int j = 0; j < group.getSpecimens().size(); j++) {
                Specimen specimen = group.getSpecimens().get(j);
                sheet.addCell(new Label(j + 2, 0, specimen.getNumber(), labelFormat));
            }

            // render group code title
            for (int j = 0; j < group.getCodeGroups().size(); j++) {
                CodeGroup codeGroup = group.getCodeGroups().get(j);
                sheet.addCell(new Label(j + group.getSpecimens().size() + 3, 0, codeGroup.getName(), labelFormat));
            }

            // render details title
            sheet.addCell(new Label(group.getSpecimens().size() + 3 + group.getCodeGroups().size() + 1, 0, "试剂批号", labelFormat));
            sheet.addCell(new Label(group.getSpecimens().size() + 3 + group.getCodeGroups().size() + 2, 0, "试剂有效期", labelFormat));
        }

        workbook.write();
        workbook.close();
    }


    @Override
    public void generateAssessmentCertification(CertificationModel certModel, OutputStream outputStream) throws IOException {
        CertificationGenerator certGenerator = new ImageCertificationGenerator(ImageCertificationGenerator.Style.DEFAULT);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        certModel.setUrl(certServer + "default/assess/assessment/certification?preview=true&certContent=" + certModel.getContent() + "&certCommentLabel=" + certModel.getCommentLabel() + "&certCommentContent=" + certModel.getCommentContent() + "&certTitle=" + certModel.getTitle() + "&certSubTitle=" + certModel.getSubTitle() + "&certIssuer=" + certModel.getIssuer()+"&certIssueDate=" + simpleDateFormat.format(certModel.getDate()));
        certGenerator.printCertification(certModel, outputStream);
    }

    @Override
    public List<Article> getArticles(String id) {
        return get(id).getArticles();
    }

    @Override
    public void removeArticles(Assessment assessment) {
        if (assessment.getArticles() != null) {
            assessment.getArticles().forEach(article -> {
                articleReaderRepository.removeArticlesReaderByArticleId(article.getId());
                articleRepository.delete(article);
            });
        }
    }

    @Override
    public Article saveArticle(String id, Article article) {
        Assessment assessment = get(id);
        article.setDate(new Date());
        article.setAssessmentId(id);
        article.setAuthorName(assessment.getOwnerName());
        article.setAuthorId(assessment.getOwner());
        if (assessment != null) {
            articleRepository.save(article);
            if (article.getId() != null
                    && !article.getId().isEmpty()) {
                boolean alreadySaved
                        = assessment.getArticles().stream().filter(a -> a!= null && a.getId().equals(article.getId())).findAny().isPresent();
                if (!alreadySaved) {
                    assessment.getArticles().add(article);
                    assessment.getParticipants().keySet().forEach(p -> articleReaderRepository.save(new ArticleReader(article, p)));
                    save(assessment);
                }
            }
        }

        return article;
    }

    @Override
    public Article removeArticle(String id, String articleId) {
        Assessment assessment = get(id);
        Article article = articleRepository.findOne(articleId);
        if (assessment != null) {
            articleRepository.delete(article);
            assessment.getArticles().remove(article);
            articleReaderRepository.removeArticlesReaderByArticleId(articleId);
            save(assessment);
        }
        return article;
    }

    @Override
    public List<Asset> getAssets(String id) {
        Assessment assessment = assessmentRepository.findOne(id);
        if (assessment != null) return assessment.getAssets();
        return new ArrayList();
    }

    @Override
    public Asset addAsset(String id, String title, MultipartFile file) throws IOException {
        Assessment assessment = assessmentRepository.findOne(id);
        if (assessment != null) {
            String url = (new OSSBucketAccessor()).put("assess-bucket", "notice-assets/" + id + "/" + file.getOriginalFilename(), file.getInputStream());
            Asset asset = new Asset();
            asset.setUrl(url);
            asset.setTitle(title);
            assetRepository.save(asset);
            assessment.getAssets().add(asset);
            assessmentRepository.save(assessment);
            return asset;
        }
        return null;
    }

    @Override
    public Asset removeAsset(String id, String assetId) {
        Assessment assessment = get(id);
        if (assessment != null) {
            Asset asset = assessment.getAssets().stream().filter(a -> a.getId().equals(assetId)).findFirst().orElseGet(null);
            if (asset != null) {
                assessment.getAssets().remove(asset);
                save(assessment);
                assetRepository.delete(asset);
                return asset;
            }
        }
        return null;
    }

    @Override
    public void removeAssets(Assessment assessment) {
        if (assessment.getAssets() != null) {
            assessment.getAssets().forEach(asset -> {
                assetRepository.delete(asset);
            });
        }
    }

    private Double calculateScore(ExpectionOption expectation, ActualValue av) {
        if (expectation != null) {
            try {
                Map<String, ScoreCalculator> calculators = (Map<String, ScoreCalculator>)applicationContext.getBean("scoreCalculators");
                ScoreCalculator calculator = calculators.get(expectation.getType());
                return calculator.calculate(expectation, av);
            } catch (Exception e){
                return new Double(0);
            }
        }

        return new Double(0);
    }

    @Override
    protected DataRepository getRepository() {
        return assessmentRepository;
    }
}
