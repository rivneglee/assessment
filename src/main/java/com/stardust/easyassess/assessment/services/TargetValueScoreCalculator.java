package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.ExpectedValue;
import com.stardust.easyassess.assessment.models.form.ExpectionOption;
import com.stardust.easyassess.assessment.models.form.OptionParameter;

public class TargetValueScoreCalculator implements ScoreCalculator {
    @Override
    public Double calculate(ExpectionOption option, ActualValue value) {

        if (option.getParameters() != null && option.getParameters().size() >= 2) {
            if (option.getExpectedValues() != null && option.getExpectedValues().size() > 0) {
                ExpectedValue expectedValue = option.getExpectedValues().get(0);
                OptionParameter up = option.getParameters().get(0);
                OptionParameter down = option.getParameters().get(1);

                Double target = new Double(expectedValue.getValue());
                Double actual = new Double(value.getValue());

                Double max = target + (target * (up.getValue()/100));
                Double min = target - (target * (down.getValue()/100));

                if (actual <= max && actual >= min) {
                    return new Double(expectedValue.getWeight());
                }
            }
        }
        return new Double(0);
    }
}