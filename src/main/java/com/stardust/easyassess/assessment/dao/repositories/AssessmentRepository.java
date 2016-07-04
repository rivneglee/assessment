package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.Assessment;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AssessmentRepository extends DataRepository<Assessment, String> {
    default Class<Assessment> getEntityClass() {
        return Assessment.class;
    }

    List<Assessment> findAssessmentsByOwner(String owner);

    @Query("{participants.?0:{$exists:true}}")
    List<Assessment> findByParticipant(Long participant);
}
