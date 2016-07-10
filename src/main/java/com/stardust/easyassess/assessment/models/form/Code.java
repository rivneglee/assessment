package com.stardust.easyassess.assessment.models.form;


public class Code extends FormElement {
    private TestSubject subject;
    private String subjectGuid;
    private String codeNumber;
    private String codeName;
    private String codeGroupName;
    private String codeGroupId;

    public TestSubject getSubject() {
        return subject;
    }

    public void setSubject(TestSubject subject) {
        this.subject = subject;
    }

    public String getSubjectGuid() {
        return subjectGuid;
    }

    public void setSubjectGuid(String subjectGuid) {
        this.subjectGuid = subjectGuid;
    }

    public String getCodeNumber() {
        return codeNumber;
    }

    public void setCodeNumber(String codeNumber) {
        this.codeNumber = codeNumber;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getCodeGroupName() {
        return codeGroupName;
    }

    public void setCodeGroupName(String codeGroupName) {
        this.codeGroupName = codeGroupName;
    }

    public String getCodeGroupId() {
        return codeGroupId;
    }

    public void setCodeGroupId(String codeGroupId) {
        this.codeGroupId = codeGroupId;
    }
}
