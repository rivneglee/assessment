package com.stardust.easyassess.assessment.common;


import com.stardust.easyassess.core.query.Selection;
import com.stardust.easyassess.core.query.SelectionQueryProvider;

import javax.persistence.criteria.*;

public class PredicateQueryProvider implements SelectionQueryProvider<Predicate> {
    private final Root<?> root;

    private final CriteriaBuilder cb;

    public PredicateQueryProvider(Root<?> root, CriteriaBuilder cb) {
        this.root = root;
        this.cb = cb;
    }

    @Override
    public Predicate toQueryObject(Selection selection) {
        if (selection.getProperty().contains(".")) {
            String [] p = selection.getProperty().split("\\.");
            String property = p[0];
            String sub = p[1];
            Join<?, ?> join = root.join(property, JoinType.LEFT);
            return this.toQueryObject(cb, join.get(sub), selection);
        } else {
            return this.toQueryObject(cb, root.get(selection.getProperty()), selection);
        }
    }

    private Predicate toQueryObject(CriteriaBuilder cb, Path<String> namePath, Selection selection) {
        Predicate predicate = null;

        switch (selection.getOperator()) {
            case NOT_EQUAL:
                predicate = cb.notEqual(namePath, selection.getValue());
                break;
            case GREATER:
                predicate = cb.greaterThan(namePath, namePath.getJavaType().cast(selection.getValue()));
                break;
            case LESS:
                predicate = cb.lessThan(namePath, namePath.getJavaType().cast(selection.getValue()));
                break;
            case GREATER_EQUAL:
                predicate = cb.greaterThanOrEqualTo(namePath, namePath.getJavaType().cast(selection.getValue()));
                break;
            case LESS_EQUAL:
                predicate = cb.lessThanOrEqualTo(namePath, namePath.getJavaType().cast(selection.getValue()));
                break;
            case LIKE:
                if (namePath.getJavaType().equals(String.class)) {
                    predicate = cb.like(namePath, "%" + selection.getValue() + "%");
                } else {
                    predicate = cb.equal(namePath, selection.getValue());
                }
                break;
            case IS_NULL:
                predicate = cb.isNull(namePath);
                break;
            case EQUAL:
            default:
                predicate = cb.equal(namePath, selection.getValue());
                break;
        }

        if (selection.getOperand().equals(Selection.Operand.AND)) {
            cb.and(predicate);
        } else if (selection.getOperand().equals(Selection.Operand.OR)) {
            cb.or(predicate);
        }

        return predicate;
    }
}
