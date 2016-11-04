package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.*;
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

        workbook.write();
        workbook.close();
    }
}
