package com.stardust.easyassess.assessment.models.form;


public class ActualValue {
    private String specimenCode;
    private String specimenGuid;
    private String subjectCode;
    private String subjectGuid;
    private String value;

    public String getSpecimenCode() {
        return specimenCode;
    }

    public void setSpecimenCode(String specimenCode) {
        this.specimenCode = specimenCode;
    }

    public String getSpecimenGuid() {
        return specimenGuid;
    }

    public void setSpecimenGuid(String specimenGuid) {
        this.specimenGuid = specimenGuid;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectGuid() {
        return subjectGuid;
    }

    public void setSubjectGuid(String subjectGuid) {
        this.subjectGuid = subjectGuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
