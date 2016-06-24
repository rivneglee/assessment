package com.stardust.easyassess.assessment.models.form;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "templates")
public class FormTemplate extends FormElement {

	@Id
	private String id;

	private String guid;

	private FormHeader header;

	private List<GroupSection> groups;
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFormName() {
		return header.getName();
	}

	public FormHeader getHeader() {
		return header;
	}

	public void setHeader(FormHeader header) {
		this.header = header;
	}

	public List<GroupSection> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupSection> groups) {
		this.groups = groups;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
