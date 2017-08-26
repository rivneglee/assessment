package com.stardust.easyassess.assessment.models.form;

import java.util.HashMap;
import java.util.Map;

public class GroupRow extends FormElement {
	
	private String guid;
	
	private TestSubject item;

	private boolean disableCodeGroup;

	public boolean isDisableCodeGroup() {
		return disableCodeGroup;
	}

	public void setDisableCodeGroup(boolean disableCodeGroup) {
		this.disableCodeGroup = disableCodeGroup;
	}

	private Map<String, ExpectionOption> optionMap = new HashMap<String, ExpectionOption>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public TestSubject getItem() {
		return item;
	}

	public void setItem(TestSubject item) {
		this.item = item;
	}

	public Map<String, ExpectionOption> getOptionMap() {
		return optionMap;
	}

	public void setOptionMap(Map<String, ExpectionOption> optionMap) {
		this.optionMap = optionMap;
	}

}
