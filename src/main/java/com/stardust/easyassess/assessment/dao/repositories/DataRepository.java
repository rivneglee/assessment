package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.common.QueryCriteriaProvider;
import com.stardust.easyassess.assessment.dao.MongoTemplateFactory;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface DataRepository<T, ID extends Serializable> extends MongoRepository<T, ID> {
    Class<T> getEntityClass();

    default Page<T> findAllBy(Pageable page, List<Selection> selections) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        QueryCriteriaProvider qcp = new QueryCriteriaProvider(criteria);

        for (Selection selection : selections) {
            if (selection.getProperty() == null
                    || selection.getProperty().isEmpty()) continue;
            if (!selection.getOperator().equals(Selection.Operator.IS_NULL) && (selection.getValue() == null
                    || selection.getValue().toString().isEmpty())) continue;

            criteria = qcp.toQueryObject(selection);
        }

        query.with(page.getSort());
        query.addCriteria(criteria);
        Long count =  MongoTemplateFactory.get().count(query, getEntityClass());
        List<T> list = MongoTemplateFactory.get().find(query.with(page), getEntityClass());
        Page<T> pagelist = new PageImpl<T>(list, page, count);
        return pagelist;
    }
}