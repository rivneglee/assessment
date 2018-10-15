package com.stardust.easyassess.assessment.models.form;


public class ExpectedValue extends FormElement {
	
	private String value;
	
	private double weight;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return value;
	}
}
