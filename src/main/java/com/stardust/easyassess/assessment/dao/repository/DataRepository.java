package com.stardust.easyassess.assessment.dao.repository;

import com.stardust.easyassess.assessment.common.PredicateQueryProvider;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


public interface DataRepository<T> extends PagingAndSortingRepository<T, String> {

    Page<T> findAll(Specification<T> spec, Pageable pageable);

    List<T> findAll(Specification<T> spec);

    T findOne(Specification<T> spec);

    default Page<T> findAll(Pageable page, String field, String value) {
        return this.findAll((root, query, cb) -> {
            Path<String> namePath = root.get(field);
            query.where(cb.equal(namePath, value));
            return query.getRestriction();
        }, page);
    }

    default Page<T> findAll(Pageable page, List<Selection> selections) {

        return this.findAll((root, query, cb) -> {
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
