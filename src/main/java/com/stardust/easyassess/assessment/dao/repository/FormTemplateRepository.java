package com.stardust.easyassess.assessment.dao.repository;

import com.stardust.easyassess.assessment.models.form.FormHeader;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FormTemplateRepository extends PagingAndSortingRepository<FormHeader, String> {

}
