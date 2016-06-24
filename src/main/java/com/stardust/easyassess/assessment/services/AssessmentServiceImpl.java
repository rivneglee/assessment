package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.AssessmentRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.Form;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Scope("request")
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    FormRepository formRepository;

    @Transactional
    public void create(Assessment assessment) {
        assessment.setStatus("A");
        assessment.setId(UUID.randomUUID().toString());

        List<Form> forms = new ArrayList<Form>();
        for (String participant : assessment.getParticipants().keySet()) {
            Form form = new Form();
            form.setOwner(participant);
            form.setAssessment(assessment);
            forms.add(form);
            assessment.getForms().add(form);
            formRepository.save(form);
        }

        assessmentRepository.save(assessment);
    }

    @Override
    public List<Assessment> findByOwner(String owner) {
        return assessmentRepository.findAssessmentsByOwner(owner);
    }

    @Override
    public List<Form> findFormsByAssessment(String id) {
        return formRepository.findFormsByAssessmentId(id);
    }
}
