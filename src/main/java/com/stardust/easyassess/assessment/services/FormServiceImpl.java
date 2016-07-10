package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormTemplateRepository;
import com.stardust.easyassess.assessment.models.form.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Scope("request")
public class FormServiceImpl extends AbstractEntityService<Form> implements FormService {

    @Autowired
    FormRepository formRepository;

    @Autowired
    FormTemplateRepository templateRepository;

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
            Map<String, List<String>> codeMap = form.getAssessment().getSpecimenCodes();
            for (ActualValue value : form.getValues()) {
                for (String specimenNumber : codeMap.keySet()) {
                    for (String specimenCode : codeMap.get(specimenNumber)) {
                        if (specimenCode.equals(value.getSpecimenCode())) {
                            value.setSpecimenNumber(specimenNumber);
                            break;
                        }
                    }
                    if (value.getSpecimenNumber() != null
                            && !value.getSpecimenNumber().isEmpty()) {
                        for (GroupSection group : template.getGroups()) {
                            for (Specimen specimen : group.getSpecimens()) {
                                if (specimen.getNumber().equals(value.getSpecimenNumber())) {
                                    value.setSpecimenGuid(specimen.getGuid());
                                }
                            }
                            for (GroupRow row : group.getRows()) {
                                if (row.getGuid().equals(value.getSubjectGuid())) {
                                    value.setSubject(row.getItem());
                                }
                            }
                        }
                        break;
                    }
                }
            }

            form.setSubmitDate(new Date());
            this.save(form);
        }
        return form;
    }
}
