package com.stardust.easyassess.assessment.models.form;

import java.util.HashMap;
import java.util.Map;

public class GroupRow extends FormElement {
	
	private String guid;
	
	private String subject;
	
	private Map<String, ExpectionOption> optionMap = new HashMap<String, ExpectionOption>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Map<String, ExpectionOption> getOptionMap() {
		return optionMap;
	}

	public void setOptionMap(Map<String, ExpectionOption> optionMap) {
		this.optionMap = optionMap;
	}

}
