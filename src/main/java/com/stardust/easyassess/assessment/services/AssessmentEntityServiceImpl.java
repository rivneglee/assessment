package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.AssessmentRepository;
import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
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
public class AssessmentEntityServiceImpl extends AbstractEntityService<Assessment> implements AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    FormRepository formRepository;

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
    protected DataRepository getRepository() {
        return assessmentRepository;
    }
}
