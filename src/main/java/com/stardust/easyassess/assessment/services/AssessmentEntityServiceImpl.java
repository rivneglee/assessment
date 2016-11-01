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
                    if (form.getStatus().equals("C")) {
                        form.setStatus("F");
                    }
                    formRepository.save(form);
                }
            }
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
        for (int i = 0; i < template.getGroups().size(); i++) {
            GroupSection group = template.getGroups().get(i);
            WritableSheet sheet = workbook.createSheet(group.getName(), i);
            // append specimens (j)
            for (int j = 0; j < group.getSpecimens().size(); j++) {
                Specimen specimen = group.getSpecimens().get(j);
                // append specimen number
                sheet.addCell(new Label(j + 2, 0, specimen.getNumber(), labelFormat));
                int groupLineCount = 0;
                // append rows (x)
                for (int x = 0; x < group.getRows().size(); x++) {
                    GroupRow row = group.getRows().get(x);
                    // append forms (y)
                    for (int y = 0; y < assessment.getForms().size(); y++) {
                        groupLineCount++;
                        Form form = assessment.getForms().get(y);
                        // append owner
                        sheet.addCell(new Label(1, groupLineCount, form.getOwnerName(), labelFormat));
                        // append values
                        for (ActualValue value : form.getValues()) {
                            if (value.getSpecimenGuid().equals(specimen.getGuid())
                                    && value.getSubjectGuid().equals(row.getGuid())) {
                                WritableCell valueCell = new Label(j + 2, groupLineCount, value.getValue(), labelFormat);
                                WritableCellFeatures cellFeatures = new WritableCellFeatures();
                                cellFeatures.setComment("盲样码:" + value.getSpecimenCode());
                                valueCell.setCellFeatures(cellFeatures);
                                sheet.addCell(valueCell);
                                break;
                            }
                        }
                    }
                    if (j == 0) {
                        sheet.addCell(new Label(0, groupLineCount - (assessment.getForms().size() - 1), row.getItem().getSubject() + "-" + row.getItem().getUnit(), labelFormat));
                        sheet.mergeCells(0, groupLineCount - (assessment.getForms().size() - 1), 0, groupLineCount);
                    }
                }


            }

            // code groups
            for (int j = 0; j < group.getCodeGroups().size(); j++) {
                CodeGroup codeGroup = group.getCodeGroups().get(j);
                sheet.addCell(new Label(group.getSpecimens().size() + 4 + j, 0, codeGroup.getName(), labelFormat));
                int groupLineCount = 0;
                // append rows (x)
                for (int x = 0; x < group.getRows().size(); x++) {
                    GroupRow row = group.getRows().get(x);
                    for (int y = 0; y < assessment.getForms().size(); y++) {
                        groupLineCount++;
                        Form form = assessment.getForms().get(y);
                        for (Code code : form.getCodes()) {
                            if (code.getCodeGroup().getGuid().equals(codeGroup.getGuid())
                                    && code.getSubjectGuid().equals(row.getGuid())) {
                                sheet.addCell(new Label(j + 4 + group.getSpecimens().size(), groupLineCount, code.getCodeName(), labelFormat));
                                break;
                            }
                        }
                    }
                }
            }
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
                return new Double(-1);
            }
        }

        return new Double(0);
    }

    @Override
    protected DataRepository getRepository() {
        return assessmentRepository;
    }
}
