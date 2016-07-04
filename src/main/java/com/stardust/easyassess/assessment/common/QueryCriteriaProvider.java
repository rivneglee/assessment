package com.stardust.easyassess.assessment.common;


import com.stardust.easyassess.core.query.Selection;
import com.stardust.easyassess.core.query.SelectionQueryProvider;
import org.springframework.data.mongodb.core.query.Criteria;

public class QueryCriteriaProvider implements SelectionQueryProvider<Criteria> {
    private Criteria criteria;

    public QueryCriteriaProvider(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Criteria toQueryObject(Selection selection) {
        Criteria joinCriteria = new Criteria(selection.getProperty());
        switch (selection.getOperator()) {
            case NOT_EQUAL:
                joinCriteria.ne(selection.getValue());
                break;
            case GREATER:
                joinCriteria.gt(selection.getValue());
                break;
            case LESS:
                joinCriteria.lt(selection.getValue());
                break;
            case GREATER_EQUAL:
                joinCriteria.gte(selection.getValue());
                break;
            case LESS_EQUAL:
                joinCriteria.lte(selection.getValue());
                break;
            case LIKE:
                joinCriteria.regex(".*?"+selection.getValue()+".*");
                break;
            case IS_NULL:
                joinCriteria.is(null);
                break;
            case NOT_NULL:
                joinCriteria.is(null).not();
                break;
            case EXSITS:
                joinCriteria.exists((Boolean) selection.getValue());
                break;
            case EQUAL:
            default:
                joinCriteria.is(selection.getValue());
                break;
        }

        if (criteria.getCriteriaObject().keySet().isEmpty()) {
            if (selection.getOperand().equals(Selection.Operand.OR)) {
                criteria.orOperator(joinCriteria);
            } else {
                criteria.andOperator(joinCriteria);
            }
        } else {
            Criteria criteriaChain = new Criteria();
            if (selection.getOperand().equals(Selection.Operand.OR)) {
                criteriaChain.orOperator(criteria, joinCriteria);
            } else {
                criteriaChain.andOperator(criteria, joinCriteria);
            }
            criteria = criteriaChain;
        }

        return criteria;
    }
}
