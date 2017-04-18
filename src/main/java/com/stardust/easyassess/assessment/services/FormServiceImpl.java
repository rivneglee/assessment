package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.common.OSSBucketAccessor;
import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.*;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

@Service
@Scope("request")
public class FormServiceImpl extends AbstractEntityService<Form> implements FormService {

    @Autowired
    FormRepository formRepository;

    @Autowired
    FormTemplateRepository templateRepository;

    @Autowired
    FormTemplateService formTemplateService;

    @Override
    protected DataRepository getRepository() {
        return formRepository;
    }

    @Override
    public Form save(Form form) {
        Form existing = this.get(form.getId());
        if (existing != null) {
            form.setAssessment(existing.getAssessment());
        }
        return super.save(form);
    }

    @Transactional
    @Override
    public Form submit(Form form) {
        if (form != null && form.getStatus().equals("A")) {

            FormTemplate template = templateRepository.findOne(form.getAssessment().getTemplateGuid());
            form.setStatus("C");
            //Map<String, List<String>> codeMap = form.getAssessment().getSpecimenCodes();
            for (ActualValue value : form.getValues()) {
                for (GroupSection group : template.getGroups()) {
                    for (Specimen specimen : group.getSpecimens()) {
                        if (specimen.getGuid().equals(value.getSpecimenGuid())) {
                            value.setSpecimenNumber(specimen.getNumber());
                            break;
                        }
                    }
                    for (GroupRow row : group.getRows()) {
                        if (row.getGuid().equals(value.getSubjectGuid())) {
                            value.setSubject(row.getItem());
                            break;
                        }
                    }
                }
            }

//            for (ActualValue value : form.getValues()) {
//                for (String specimenNumber : codeMap.keySet()) {
//                    for (String specimenCode : codeMap.get(specimenNumber)) {
//                        if (value.getSpecimenCode().contains("+")) {
//                            if (Arrays.asList(value.getSpecimenCode().split("\\+")).contains(specimenCode)) {
//                                if (value.getSpecimenNumber() == null || value.getSpecimenNumber().isEmpty()) {
//                                    value.setSpecimenNumber(specimenNumber);
//                                } else {
//                                    value.setSpecimenNumber(value.getSpecimenNumber() + "+" + specimenNumber);
//                                }
//                                break;
//                            }
//                        } else if (specimenCode.equals(value.getSpecimenCode())) {
//                            value.setSpecimenNumber(specimenNumber);
//                            break;
//                        }
//                    }
//                }
//
//                if (value.getSpecimenNumber() != null
//                        && !value.getSpecimenNumber().isEmpty()) {
//
//                    Set<String> specimenNumberSet = new HashSet();
//                    if (value.getSpecimenNumber().contains("+")) {
//                        specimenNumberSet.addAll(Arrays.asList(value.getSpecimenNumber().split("\\+")));
//                    } else {
//                        specimenNumberSet.add(value.getSpecimenNumber());
//                    }
//
//                    for (GroupSection group : template.getGroups()) {
//                        for (Specimen specimen : group.getSpecimens()) {
//                            if (specimen.getNumber().contains("+")) {
//                                if (specimenNumberSet.containsAll(Arrays.asList(specimen.getNumber().split("\\+")))) {
//                                    value.setSpecimenGuid(specimen.getGuid());
//                                }
//                            } else {
//                                if (specimenNumberSet.contains(specimen.getNumber())) {
//                                    value.setSpecimenGuid(specimen.getGuid());
//                                }
//                            }
//                        }
//                        for (GroupRow row : group.getRows()) {
//                            if (row.getGuid().equals(value.getSubjectGuid())) {
//                                value.setSubject(row.getItem());
//                            }
//                        }
//                    }
//                }
//            }

            form.setSubmitDate(new Date());
            this.save(form);
        }
        return form;
    }
/*
    @Override
    public void exportToExcel(Form form, OutputStream outputStream) throws IOException, WriteException {
        FormTemplate template = formTemplateService.get(form.getAssessment().getTemplateGuid());
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
            Map<String, String> specimenNumberCodeMap = new HashMap();
            // render rows
            for (int j = 0; j < group.getRows().size(); j++) {
                GroupRow row = group.getRows().get(j);
                sheet.addCell(new Label(0, j + 1, row.getItem().getSubject() + "-" + row.getItem().getUnit(), labelFormat));
                // render values
                for (int k = 0; k < group.getSpecimens().size(); k++) {
                    Specimen specimen = group.getSpecimens().get(k);
                    for (ActualValue value : form.getValues()) {
                        if (value.getSpecimenGuid().equals(specimen.getGuid())
                                && value.getSubjectGuid().equals(row.getGuid())) {
                            WritableCell valueCell;
                            if (value.getScore() != null && value.getScore().compareTo(new Double(100 / group.getSpecimens().size())) == -1) {
                                WritableFont redFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
                                redFont.setColour(Colour.RED);
                                WritableCellFormat redFormat = new WritableCellFormat(redFont);
                                redFormat.setAlignment(Alignment.CENTRE);
                                valueCell = new Label(k + 1, j + 1, value.getValue(), redFormat);
                            } else {
                                valueCell = new Label(k + 1, j + 1, value.getValue(), labelFormat);
                            }
                            if (row.getOptionMap() != null && row.getOptionMap().containsKey(specimen.getGuid())) {
                                ExpectionOption eo = row.getOptionMap().get(specimen.getGuid());
                                WritableCellFeatures cellFeatures = new WritableCellFeatures();
                                if (eo.getExpectedValues() != null && !eo.getExpectedValues().isEmpty()) {
                                    cellFeatures.setComment("标准值:" + Arrays.toString(eo.getExpectedValues().toArray()));
                                    valueCell.setCellFeatures(cellFeatures);
                                }
                            }

                            sheet.addCell(valueCell);
                            specimenNumberCodeMap.put(value.getSpecimenNumber(), value.getSpecimenCode());
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
                            sheet.addCell(new Label(k + group.getSpecimens().size() + 2, j + 1, code.getCodeName(), labelFormat));
                            break;
                        }
                    }
                }
                for (Map<String, String> details : form.getDetails()) {
                    if (details.get("subjectGuid").equals(row.getGuid())) {
                        int k = 0;
                        for (String key : details.keySet()) {
                            k++;
                            if (key.equals("subjectGuid")) continue;
                            sheet.addCell(new Label(k + group.getSpecimens().size() + group.getCodeGroups().size() + 1, j + 1, details.get(key), labelFormat));
                        }
                        break;
                    }
                }
            }

            // render specimen title
            for (int j = 0; j < group.getSpecimens().size(); j++) {
                Specimen specimen = group.getSpecimens().get(j);
                sheet.addCell(new Label(j + 1, 0, specimen.getNumber() + "(" + specimenNumberCodeMap.get(specimen.getNumber()) + ")", labelFormat));
            }

            // render group code title
            for (int j = 0; j < group.getCodeGroups().size(); j++) {
                CodeGroup codeGroup = group.getCodeGroups().get(j);
                sheet.addCell(new Label(j + group.getSpecimens().size() + 2, 0, codeGroup.getName(), labelFormat));
            }

            // 试剂批号/有效期
            sheet.addCell(new Label(group.getSpecimens().size() + group.getCodeGroups().size() + 3, 0, "试剂批号", labelFormat));
            sheet.addCell(new Label(group.getSpecimens().size() + group.getCodeGroups().size() + 4, 0, "试剂有效期", labelFormat));

            if (form.getSignatures().get(group.getGuid()) != null) {
                // 签名
                sheet.addCell(new Label(0, group.getRows().size() + 2, "检测人:", labelFormat));
                sheet.addCell(new Label(1, group.getRows().size() + 2, form.getSignatures().get(group.getGuid()).get("tester"), labelFormat));
                sheet.addCell(new Label(3, group.getRows().size() + 2, "检测日期:", labelFormat));
                sheet.addCell(new Label(4, group.getRows().size() + 2, form.getSignatures().get(group.getGuid()).get("testDate"), labelFormat));
                sheet.addCell(new Label(0, group.getRows().size() + 3, "审核人:", labelFormat));
                sheet.addCell(new Label(1, group.getRows().size() + 3, form.getSignatures().get(group.getGuid()).get("reviewer"), labelFormat));
                sheet.addCell(new Label(3, group.getRows().size() + 3, "检测日期:", labelFormat));
                sheet.addCell(new Label(4, group.getRows().size() + 3, form.getSignatures().get(group.getGuid()).get("reviewDate"), labelFormat));

                //备注
                sheet.addCell(new Label(0, group.getRows().size() + 4, "备注:", labelFormat));
                sheet.addCell(new Label(0, group.getRows().size() + 5, form.getSignatures().get(group.getGuid()).get("comments"), labelFormat));
            }

        }

        workbook.write();
        workbook.close();
    }*/

    @Override
    public void exportToExcel(Form form, OutputStream outputStream) throws IOException, WriteException {
        FormTemplate template = formTemplateService.get(form.getAssessment().getTemplateGuid());
        WritableWorkbook workbook = Workbook.createWorkbook(outputStream);
        WritableFont boldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 15, WritableFont.BOLD);

        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setBorder(Border.NONE, BorderLineStyle.NONE);
        headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        headerFormat.setAlignment(Alignment.CENTRE);
        headerFormat.setWrap(true);

        WritableCellFormat sectionFormat = new WritableCellFormat(boldFont);
        sectionFormat.setBorder(Border.NONE, BorderLineStyle.NONE);
        sectionFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        sectionFormat.setAlignment(Alignment.CENTRE);
        sectionFormat.setWrap(true);
        sectionFormat.setBackground(Colour.GREY_25_PERCENT);

        WritableCellFormat titleFormat = new WritableCellFormat(boldFont);
        titleFormat.setBorder(Border.NONE, BorderLineStyle.NONE);
        titleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        titleFormat.setAlignment(Alignment.CENTRE);
        titleFormat.setWrap(true);

        WritableCellFormat labelFormat = new WritableCellFormat(normalFont);
        labelFormat.setBorder(Border.NONE, BorderLineStyle.NONE);
        labelFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        labelFormat.setAlignment(Alignment.LEFT);
        labelFormat.setWrap(true);
        WritableSheet sheet = workbook.createSheet("结果", 0);

        //header
        sheet.addCell(new Label(0, 0, form.getAssessment().getName(), headerFormat));
        sheet.mergeCells(0, 0, 5, 0);

        //lab info
        sheet.addCell(new Label(0, 1, "实验室名称:", titleFormat));
        sheet.addCell(new Label(1, 1, form.getOwnerName(), labelFormat));
        sheet.addCell(new Label(3, 1, "提交日期:", titleFormat));
        sheet.addCell(new Label(4, 1, form.getSubmitDate().toString(), labelFormat));
        sheet.mergeCells(4, 1, 5, 1);

        // score
        sheet.addCell(new Label(3, 2, "总分:", titleFormat));
        sheet.addCell(new Label(4, 2, form.getTotalScore().toString(), labelFormat));
        sheet.mergeCells(4, 2, 5, 2);

        int currentRow = 4;
        for (int i = 0; i < template.getGroups().size(); i++) {
            GroupSection group = template.getGroups().get(i);
            sheet.addCell(new Label(0, currentRow, group.getName(), sectionFormat));
            sheet.mergeCells(0, currentRow, 5, currentRow++);
            // render rows
            for (int j = 0; j < group.getRows().size(); j++) {
                GroupRow row = group.getRows().get(j);
                sheet.addCell(new Label(0, currentRow, row.getItem().getSubject(), titleFormat));
                sheet.mergeCells(0, currentRow, 0, group.getSpecimens().size() + currentRow);
                sheet.addCell(new Label(1, currentRow, "样本代码", titleFormat));
                sheet.addCell(new Label(2, currentRow, "盲样码", titleFormat));
                sheet.addCell(new Label(3, currentRow, "你室结果", titleFormat));
                sheet.addCell(new Label(4, currentRow, "正确结果", titleFormat));
                sheet.addCell(new Label(5, currentRow, "分数", titleFormat));
                currentRow++;
                // render values
                Double sujectScore = new Double(0);
                for (int k = 0; k < group.getSpecimens().size(); k++) {
                    Specimen specimen = group.getSpecimens().get(k);
                    sheet.addCell(new Label(1, currentRow, specimen.getNumber(), labelFormat));
                    for (ActualValue value : form.getValues()) {
                        if (value.getSpecimenGuid().equals(specimen.getGuid())
                                && value.getSubjectGuid().equals(row.getGuid())) {
                            sheet.addCell(new Label(2, currentRow, value.getSpecimenCode(), labelFormat));
                            sheet.addCell(new Label(3, currentRow, value.getValue(), labelFormat));
                            Double score = value.getScore() == null ? 0 : value.getScore();
                            sheet.addCell(new Label(5, currentRow, score.toString() , labelFormat));
                            sujectScore += score;
                            if (row.getOptionMap() != null && row.getOptionMap().containsKey(specimen.getGuid())) {
                                ExpectionOption eo = row.getOptionMap().get(specimen.getGuid());
                                sheet.addCell(new Label(4, currentRow, Arrays.toString(eo.getExpectedValues().toArray()), labelFormat));
                            }
                            break;
                        }
                    }
                    currentRow++;
                }

                sheet.addCell(new Label(4, currentRow, "检测项得分:", titleFormat));
                sheet.addCell(new Label(5, currentRow++, sujectScore.toString(), labelFormat));

                // render codes
                for (int k = 0; k < group.getCodeGroups().size(); k++) {
                    CodeGroup codeGroup = group.getCodeGroups().get(k);
                    for (Code code : form.getCodes()) {
                        if (code.getCodeGroup().getGuid().equals(codeGroup.getGuid())
                                && code.getSubjectGuid().equals(row.getGuid())) {
                            sheet.addCell(new Label(1, currentRow, codeGroup.getName() + ":", titleFormat));
                            sheet.addCell(new Label(2, currentRow, code.getCodeName(), labelFormat));
                            sheet.mergeCells(2, currentRow, 5, currentRow);
                            currentRow++;
                            break;
                        }
                    }
                }

                if (form.getDetails() != null) {
                    for (Map<String, String> details : form.getDetails()) {
                        if (details.get("subjectGuid").equals(row.getGuid())) {
                            if (details.containsKey("batchNumber")) {
                                sheet.addCell(new Label(0, currentRow, "试剂批号:", titleFormat));
                                sheet.addCell(new Label(1, currentRow, details.get("batchNumber"), labelFormat));
                                sheet.mergeCells(1, currentRow, 5, currentRow++);
                            }
                            if (details.containsKey("expire")) {
                                sheet.addCell(new Label(0, currentRow, "有效期:", titleFormat));
                                sheet.addCell(new Label(1, currentRow, details.get("expire"), labelFormat));
                                sheet.mergeCells(1, currentRow, 5, currentRow++);
                            }
                            break;
                        }
                    }
                }

                if (form.getSignatures().get(group.getGuid()) != null) {
                    // 签名
                    sheet.addCell(new Label(0, currentRow, "检测人:", titleFormat));
                    sheet.addCell(new Label(1, currentRow, form.getSignatures().get(group.getGuid()).get("tester"), labelFormat));
                    sheet.addCell(new Label(3, currentRow, "检测日期:", titleFormat));
                    sheet.addCell(new Label(4, currentRow++, form.getSignatures().get(group.getGuid()).get("testDate"), labelFormat));
                    sheet.addCell(new Label(0, currentRow, "审核人:", titleFormat));
                    sheet.addCell(new Label(1, currentRow, form.getSignatures().get(group.getGuid()).get("reviewer"), labelFormat));
                    sheet.addCell(new Label(3, currentRow, "检测日期:", titleFormat));
                    sheet.addCell(new Label(4, currentRow++, form.getSignatures().get(group.getGuid()).get("reviewDate"), labelFormat));

                    //备注
                    sheet.addCell(new Label(0, currentRow, "备注:", titleFormat));
                    sheet.addCell(new Label(1, currentRow, form.getSignatures().get(group.getGuid()).get("comments"), labelFormat));
                    sheet.mergeCells(1, currentRow, 5, currentRow++);
                }
            }
        }

        sheet.setColumnView(0, 15);
        sheet.setColumnView(1, 20);
        sheet.setColumnView(2, 20);
        sheet.setColumnView(3, 15);
        sheet.setColumnView(4, 15);
        sheet.setColumnView(4, 10);
        workbook.write();
        workbook.close();
    }

    @Override
    public String addAttachment(String formId, InputStream inputStream) {
        Form form = formRepository.findOne(formId);
        if (form != null) {
           String link = (new OSSBucketAccessor()).put("assess-bucket", "assessment-attachment/" + formId, inputStream);
           if (link != null && !link.isEmpty()) {
                form.setAttachment(link);
                formRepository.save(form);
           }

           return link;
        }

        return null;
    }
}
