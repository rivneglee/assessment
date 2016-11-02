package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.*;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
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
                            sheet.addCell(new Label(k + 1, j + 1, value.getValue(), labelFormat));
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

        }

        workbook.write();
        workbook.close();
    }
}
