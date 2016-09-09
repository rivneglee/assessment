package com.stardust.easyassess.assessment.models.form;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormData extends FormElement {
    private String tester;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date testDate;

    private String reviewer;

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

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
