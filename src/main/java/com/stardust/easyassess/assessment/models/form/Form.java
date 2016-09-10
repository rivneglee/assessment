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

    private List<Code> codes = new ArrayList();

    private List<Detail> details = new ArrayList();

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

    public List<Code> getCodes() {
        return codes;
    }

    public void setCodes(List<Code> codes) {
        this.codes = codes;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
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

    public Assessment getSecuredAssessment() {
        Assessment assessment = new Assessment();
        assessment.setId(this.assessment.getId());
        assessment.setName(this.assessment.getName());
        assessment.setStartDate(this.assessment.getStartDate());
        assessment.setEndDate(this.assessment.getEndDate());
        assessment.setStatus(this.assessment.getStatus());
        assessment.setTemplateGuid(this.assessment.getTemplateGuid());
        assessment.setOwner(this.assessment.getOwner());
        assessment.setOwnerName(this.assessment.getOwnerName());
        return assessment;
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

