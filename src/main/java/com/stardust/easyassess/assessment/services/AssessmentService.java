package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AssessmentService extends EntityService<Assessment> {
    void createAssessment(Assessment assessment);

    List<Assessment> findByParticipant(String participant);

    Specimen findSpecimen(String assessmentId, String group,  String specimenCode);

    Assessment finalizeAssessment(String id);

    void finalizeAssessment(Assessment assessment);
}
