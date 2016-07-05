package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.AssessmentRepository;
import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.*;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Scope("request")
public class AssessmentEntityServiceImpl extends AbstractEntityService<Assessment> implements AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    FormRepository formRepository;

    @Autowired
    FormTemplateService formTemplateService;

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
    public Form findForm(String id) {
        return formRepository.findOne(id);
    }

    @Override
    public Form updateFormActualValues(String id, List<ActualValue> values) {
        Form form = formRepository.findOne(id);
        if (form != null) {
            form.setValues(values);
            formRepository.save(form);
        }

        return form;
    }

    @Override
    public Specimen findSpecimen(String assessmentId, String specimenCode) {
        Assessment assessment = this.get(assessmentId);
        String originalSpecimenNumber = null;
        if (assessment != null) {
            for (String specimenNumber : assessment.getSpecimenCodes().keySet()) {
                List<String> codes = assessment.getSpecimenCodes().get(specimenNumber);
                if (codes.contains(specimenCode)) {
                    originalSpecimenNumber = specimenNumber;
                    break;
                }
            }

            if (originalSpecimenNumber != null) {
                FormTemplate template = formTemplateService.get(assessment.getTemplateGuid());
                for (GroupSection groups : template.getGroups()) {
                    for (Specimen specimen : groups.getSpecimens()) {
                        if (specimen.getNumber().equals(originalSpecimenNumber)) {
                            return specimen;
                        }
                    }
                }
            }
        }

        return null;
    }


    @Override
    protected DataRepository getRepository() {
        return assessmentRepository;
    }
}
