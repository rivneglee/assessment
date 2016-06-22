package com.stardust.easyassess.assessment.models.form;

import java.util.ArrayList;
import java.util.List;


public class ExpectionOption extends FormElement {
	private String type;
	
	private List<ExpectedValue> expectedValues = new ArrayList<ExpectedValue>();
	
	private List<OptionValue> optionValues = new ArrayList<OptionValue>();

	public List<ExpectedValue> getExpectedValues() {
		return expectedValues;
	}

	public void setExpectedValues(List<ExpectedValue> expectedValues) {
		this.expectedValues = expectedValues;
	}

	public List<OptionValue> getOptionValues() {
		return optionValues;
	}

	public void setOptionValues(List<OptionValue> optionValues) {
		this.optionValues = optionValues;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
