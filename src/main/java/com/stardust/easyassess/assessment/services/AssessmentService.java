package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.Assessment;

import java.util.List;

public interface AssessmentService extends EntityService<Assessment> {
     void createAssessment(Assessment assessment);

     List<Assessment> findByParticipant(String participant);
}
