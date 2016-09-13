package com.stardust.easyassess.assessment.models.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormData extends FormElement {
    private List<ActualValue> values = new ArrayList<ActualValue>();

    private List<Map<String, String>> details = new ArrayList();

    private Map<String, Map<String, String>> signatures = new HashMap<String, Map<String, String>>();

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

    public Map<String, Map<String, String>> getSignatures() {
        return signatures;
    }

    public void setSignatures(Map<String, Map<String, String>> signatures) {
        this.signatures = signatures;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }

    public void setDetails(List<Map<String, String>> details) {
        this.details = details;
    }
}
