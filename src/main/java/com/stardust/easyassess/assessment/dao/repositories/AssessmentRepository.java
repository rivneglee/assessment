package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.Assessment;

import java.util.List;

public interface AssessmentRepository extends DataRepository<Assessment, String> {
    List<Assessment> findAssessmentsByOwner(String owner);
}
