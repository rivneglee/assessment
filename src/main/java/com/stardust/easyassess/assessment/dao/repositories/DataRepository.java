package com.stardust.easyassess.assessment.dao.repositories;

import com.stardust.easyassess.assessment.common.PredicateQueryProvider;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoRepositoryBean
public interface DataRepository<T, ID extends Serializable> extends MongoRepository<T, ID> {

    Page<T> findAllBy(Specification<?> T, Pageable pageable);

    default Page<T> findAllBy(Pageable page, List<Selection> selections) {

        return this.findAllBy((root, query, cb) -> {
            PredicateQueryProvider pqp = new PredicateQueryProvider(root, cb);

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(cb.greaterThan(root.get("id"), 0));

            for (Selection selection : selections) {
                if (selection.getProperty() == null
                        || selection.getProperty().isEmpty()) continue;
                if (!selection.getOperator().equals(Selection.Operator.IS_NULL) && (selection.getValue() == null
                        || selection.getValue().toString().isEmpty())) continue;

                predicates.add(pqp.toQueryObject(selection));
            }

            query.where(predicates.toArray(new Predicate[selections.size()]));
            return query.getRestriction();
        }, page);

    }
}