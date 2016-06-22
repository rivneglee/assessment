package com.stardust.easyassess.assessment.models.form;

import java.util.List;

public class FormTemplate extends FormElement {
	
	protected String guid;

	protected FormHeader header;
	
	protected List<GroupSection> groups;
	
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
}
