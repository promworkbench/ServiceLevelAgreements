package org.processmining.servicelevelagreements.algorithm;

import org.processmining.servicelevelagreements.model.reasoner.Reasoner;

/**
 * Abstract class for SLA algorithms.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public abstract class ServiceLevelAgreementsAlgorithm implements IServiceLevelAgreementsAlgorithm {

	// FIELDS

	protected Reasoner reasoner;

	// GETTERS AND SETTERS

	public Reasoner getReasoner() {
		return reasoner;
	}

	public void setReasoner(Reasoner reasoner) {
		this.reasoner = reasoner;
	}

}
