package org.processmining.servicelevelagreements.model.fluent;

public abstract class Fluent {

	// FIELDS

	private String name;
	private int numberOfArguments;

	// CONSTRUCTORS

	public Fluent() {

	}

	// GETTERS AND SETTERS

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfArguments() {
		return numberOfArguments;
	}

	public void setNumberOfArguments(int numberOfArguments) {
		this.numberOfArguments = numberOfArguments;
	}

}
