package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.Assessment;

public interface AssessmentService extends EntityService<Assessment> {
     void createAssessment(Assessment assessment);
}
