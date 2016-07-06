package com.stardust.easyassess.assessment.models.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stardust.easyassess.assessment.models.Assessment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "forms")
public class Form extends FormElement {

    @Id
    private String id;

    @JsonIgnore
    @DBRef
    private Assessment assessment;

    private String owner;

    private String status;

    private String formName;

    private Double totalScore;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date submitDate;

    private List<ActualValue> values = new ArrayList<ActualValue>();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<ActualValue> getValues() {
        return values;
    }

    public void setValues(List<ActualValue> values) {
        this.values = values;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public String getOwnerName() {
        return assessment.getParticipants().get(this.getOwner());
    }

    public String getAssessmentOwner() {
        return assessment.getOwner();
    }

    public String getAssessmentOwnerName() {
        return assessment.getOwnerName();
    }

    public String getTemplateId() {
        return assessment.getTemplateGuid();
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }
}

