package com.stardust.easyassess.assessment.models.form;


public class ExpectedValue extends FormElement {
	
	private String value;
	
	private int weight;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}
