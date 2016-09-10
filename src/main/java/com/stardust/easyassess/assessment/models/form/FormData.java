package com.stardust.easyassess.assessment.models.form;

import java.util.ArrayList;
import java.util.List;

public class FormData extends FormElement {
    private List<ActualValue> values = new ArrayList<ActualValue>();

    private List<Detail> details = new ArrayList();

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

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
