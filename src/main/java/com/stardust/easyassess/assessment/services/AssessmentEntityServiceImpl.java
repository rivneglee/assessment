package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.AssessmentRepository;
import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.*;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Service
@Scope("request")
public class AssessmentEntityServiceImpl extends AbstractEntityService<Assessment> implements AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    FormRepository formRepository;

    @Autowired
    FormTemplateService formTemplateService;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    @Transactional
    public void createAssessment(Assessment assessment) {
        assessment.setStatus("A");
        assessment.setId(UUID.randomUUID().toString());

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
        if (assessment.getStatus().equals("A")) {
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
                    break;
                }
            }

            if (specimenNumberSet.size() > 0) {
                FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());

                for (GroupSection groups : template.getGroups()) {
                    if (!groups.getGuid().equals(groupId)) continue;
                    for (Specimen specimen : groups.getSpecimens()) {
                        if (specimenNumberSet.size() == 1
                                && !specimen.getNumber().contains("+")
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
        // render groups
        for (int i = 0; i < template.getGroups().size(); i++) {
            GroupSection group = template.getGroups().get(i);
            WritableSheet sheet = workbook.createSheet(group.getName(), i);
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
