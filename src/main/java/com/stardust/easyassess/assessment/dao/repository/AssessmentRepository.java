package com.stardust.easyassess.assessment.dao.repository;

import com.stardust.easyassess.assessment.models.Assessment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AssessmentRepository extends PagingAndSortingRepository<Assessment, String> {

}
