package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.models.form.Form;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormRepository extends DataRepository<Form, String> {
    @Query("SELECT f FROM forms f WHERE f.assessment.id = :id")
    List<Form> findFormsByAssessmentId(String id);

    default Class<Form> getEntityClass() {
        return Form.class;
    }
}
