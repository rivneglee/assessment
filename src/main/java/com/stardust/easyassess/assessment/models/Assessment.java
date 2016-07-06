package com.stardust.easyassess.assessment.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.stardust.easyassess.assessment.models.form.Form;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Embedded;
import java.util.*;

@Document(collection = "assessments")
public class Assessment extends DataModel {
    @Id
    private String id;
    private String name;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date endDate;
    private String owner;
    private String ownerName;
    private String status;

    @Embedded
    private Map<String, String> participants;
    private String templateGuid;
    @Embedded
    private Map<String, List<String>> specimenCodes = new HashedMap();

    @DBRef
    private List<Form> forms = new ArrayList<Form>();

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, String> participants) {
        this.participants = participants;
    }

    public String getTemplateGuid() {
        return templateGuid;
    }

    public void setTemplateGuid(String templateGuid) {
        this.templateGuid = templateGuid;
    }

    public Map<String, List<String>> getSpecimenCodes() {
        return specimenCodes;
    }

    public void setSpecimenCodes(Map<String, List<String>> specimenCodes) {
        this.specimenCodes = specimenCodes;
    }
}
