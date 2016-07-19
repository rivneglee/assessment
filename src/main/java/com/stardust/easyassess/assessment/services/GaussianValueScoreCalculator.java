package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.ExpectionOption;

public class GaussianValueScoreCalculator implements ScoreCalculator {
    @Override
    public Double calculate(ExpectionOption option, ActualValue value) {
        if (option.getParameters() != null && option.getParameters().size() >=2) {
            Double x = new Double(value.getValue());
            Double micro = option.getParameters().get(0).getValue();
            Double sigma = option.getParameters().get(1).getValue();
            return gaussianExpression(x, micro, sigma);
        }

        return new Double(0);
    }

    private Double gaussianExpression(Double x, Double micro, Double sigma) {
        Double partOne = 1 / (Math.sqrt(2 * Math.PI) * sigma);
        Double partTwo = Math.exp(-(Math.pow((x - micro),2) / 2 * Math.pow(sigma, 2)));
        return partOne * partTwo;
    }
}