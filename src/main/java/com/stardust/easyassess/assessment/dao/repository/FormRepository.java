package com.stardust.easyassess.assessment.dao.repository;

import com.stardust.easyassess.assessment.models.form.Form;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FormRepository extends PagingAndSortingRepository<Form, String> {
    @Query("SELECT f FROM forms f WHERE f.assessment.id = :id")
    List<Form> findFormsByAssessmentId(String id);
}
