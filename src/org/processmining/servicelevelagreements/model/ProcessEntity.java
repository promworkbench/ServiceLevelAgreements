package org.processmining.servicelevelagreements.model;

public enum ProcessEntity {
	//@formatter:off
	ACTIVITYINSTANCE("ActivityInstance", "Activity instance process entity."), 
	ACTIVITY("Activity", "Activity concept name process entity."), 
	CASE("Case", "Case concept name process entity."), 
	RESOURCE("Resource", "Resource process entity."), 
	PROCESS("Process", "Process process entity.");
	//@formatter:on

	private String shortDescription;
	private String longDescription;

	ProcessEntity(String shortDescription, String longDescription) {
		setShortDescription(shortDescription);
		setLongDescription(longDescription);
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public static ProcessEntity parseFromText(String text) {
		switch (text) {
			case "ActivityInstance" :
				return ACTIVITYINSTANCE;
			case "Activity" :
				return ACTIVITY;
			case "Case" :
				return CASE;
			case "Resource" :
				return RESOURCE;
			case "Process" :
				return PROCESS;
		}
		return null;
	}

}
