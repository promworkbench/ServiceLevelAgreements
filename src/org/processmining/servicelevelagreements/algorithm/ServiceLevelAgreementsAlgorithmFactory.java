package org.processmining.servicelevelagreements.algorithm;

import org.processmining.servicelevelagreements.algorithm.impl.BasicServiceLevelAgreementsAlgorithm;

/**
 * Factory for SLA algorithms.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class ServiceLevelAgreementsAlgorithmFactory {

	public static ServiceLevelAgreementsAlgorithm
			getServiceLevelAgreementsAlgorithm(ServiceLevelAgreementsAlgorithmType type) {
		switch (type) {
			case BASIC :
				return new BasicServiceLevelAgreementsAlgorithm();
			default :
				return null;
		}
	}

}
