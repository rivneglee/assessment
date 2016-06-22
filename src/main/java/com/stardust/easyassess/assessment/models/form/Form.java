package com.stardust.easyassess.assessment.models.form;

import com.stardust.easyassess.assessment.models.Assessment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "forms")
public class Form extends FormElement {

    @Id
    private String id;

    @DBRef
    private Assessment assessment;

    private String templateGuid;

    private String owner;

    private List<ActualValue> values = new ArrayList<ActualValue>();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTemplateGuid() {
        return templateGuid;
    }

    public void setTemplateGuid(String templateGuid) {
        this.templateGuid = templateGuid;
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
}

