package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.ExpectionOption;

public interface ScoreCalculator {
    Double calculate(ExpectionOption option, ActualValue value);
}
