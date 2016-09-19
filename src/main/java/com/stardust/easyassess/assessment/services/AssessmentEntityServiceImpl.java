package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.AssessmentRepository;
import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.assessment.dao.repositories.FormRepository;
import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Specimen findSpecimen(String assessmentId, String specimenCode) {
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
