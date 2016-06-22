package com.stardust.easyassess.assessment.models.form;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "form_header")
public class FormHeader extends FormElement {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}