package org.processmining.servicelevelagreements.model.sde;

import com.google.common.base.Objects;

/**
 * SDE event. "_" is used as a universal quantifier.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class SDE {

	// FIELDS

	private String lifecycleTransition;
	private String activityConceptName;
	private String activityInstanceID;
	private String caseConceptName;
	private String resource;

	// CONSTRUCTORS

	public SDE() {
	}

	// GETTERS AND SETTERS

	public String getLifecycleTransition() {
		return lifecycleTransition;
	}

	public void setLifecycleTransition(String lifecycleTransition) {
		this.lifecycleTransition = lifecycleTransition;
	}

	public String getActivityConceptName() {
		return activityConceptName;
	}

	public void setActivityConceptName(String activityConceptName) {
		this.activityConceptName = activityConceptName;
	}

	public String getActivityInstanceID() {
		return activityInstanceID;
	}

	public void setActivityInstanceID(String activityInstanceID) {
		this.activityInstanceID = activityInstanceID;
	}

	public String getCaseConceptName() {
		return caseConceptName;
	}

	public void setCaseConceptName(String caseConceptName) {
		this.caseConceptName = caseConceptName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	// METHODS

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SDE))
			return false;

		SDE sde = (SDE) obj;

		if (!sde.getLifecycleTransition().equals(lifecycleTransition))
			return false;

		if (!sde.getActivityConceptName().equals(activityConceptName))
			return false;

		if (!sde.getActivityConceptName().equals(activityInstanceID))
			return false;

		if (!sde.getCaseConceptName().equals(caseConceptName))
			return false;

		if (!sde.getResource().equals(resource))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(lifecycleTransition, activityConceptName, activityInstanceID, caseConceptName,
				resource);
	}

//	@Override
//	public String toString() {
//		return String.format("%s(%s,%s,%s,%s)", lifecycleTransition, activityConceptName, activityInstanceID,
//				caseConceptName, resource);
//	}

}
