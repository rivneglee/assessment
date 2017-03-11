package com.stardust.easyassess.assessment.models.form;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "form_footer")
public class FormFooter extends FormElement {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}