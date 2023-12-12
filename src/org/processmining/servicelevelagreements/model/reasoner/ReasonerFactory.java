package org.processmining.servicelevelagreements.model.reasoner;

import org.processmining.servicelevelagreements.model.reasoner.impl.BasicDeductiveReasoner;

/**
 * Factory for reasoners.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class ReasonerFactory {

	public static Reasoner getReasoner(ReasonerType type) {
		switch (type) {
			case BASIC_DEDUCTIVE :
				return new BasicDeductiveReasoner();
			default :
				return null;
		}
	}

}
