package com.stardust.easyassess.assessment.models.form;

import java.util.ArrayList;
import java.util.List;

public class FormData extends FormElement {
    private List<ActualValue> values = new ArrayList<ActualValue>();

    private List<Code> codes = new ArrayList();

    public List<ActualValue> getValues() {
        return values;
    }

    public void setValues(List<ActualValue> values) {
        this.values = values;
    }

    public List<Code> getCodes() {
        return codes;
    }

    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }
}
