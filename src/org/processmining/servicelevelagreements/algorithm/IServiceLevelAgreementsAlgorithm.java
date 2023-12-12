package org.processmining.servicelevelagreements.algorithm;

import org.processmining.servicelevelagreements.model.reasoner.Reasoner;

/**
 * Interface to be used by SLA algorithms.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public interface IServiceLevelAgreementsAlgorithm {

	public Reasoner getReasoner();

	public void setReasoner(Reasoner reasoner);

}
