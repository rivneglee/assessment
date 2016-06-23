package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.Form;

import java.util.List;

public interface AssessmentService {
     void create(Assessment assessment);

     List<Assessment> findByOwner(String owner);

     List<Form> findFormsByAssessment(String id);
}
