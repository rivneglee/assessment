package com.stardust.easyassess.assessment.models.form;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Detail extends FormElement {
    private TestSubject subject;
    private String subjectGuid;
    private String tester;
    private String reviewer;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date testDate;

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

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }
}
