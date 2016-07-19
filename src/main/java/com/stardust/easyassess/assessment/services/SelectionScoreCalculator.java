package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.ExpectedValue;
import com.stardust.easyassess.assessment.models.form.ExpectionOption;

public class SelectionScoreCalculator implements ScoreCalculator {
    @Override
    public Double calculate(ExpectionOption option, ActualValue value) {
        if (option.getExpectedValues() != null && option.getExpectedValues().size() > 0) {
            ExpectedValue expectedValue = option.getExpectedValues().get(0);
            if (expectedValue.getValue().equals(value.getValue())) {
                return new Double(expectedValue.getWeight());
            }
        }
        return new Double(0);
    }
}
