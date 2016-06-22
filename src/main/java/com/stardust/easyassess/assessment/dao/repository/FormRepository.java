package com.stardust.easyassess.assessment.dao.repository;

import com.stardust.easyassess.assessment.models.form.Form;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FormRepository extends PagingAndSortingRepository<Form, String> {

}
