package org.processmining.servicelevelagreements.model.xes.classification;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.processmining.servicelevelagreements.model.xes.extensions.XCaseExtension;

/**
 * Implements an event classifier based on the caseid name attribute of events.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class XEventCaseClassifier extends XEventAttributeClassifier {

	private static final long serialVersionUID = -4928730645233406046L;

	/**
	 * Creates a new instance of this event classifier.
	 */
	public XEventCaseClassifier() {
		super("Case", XCaseExtension.KEY_CASE);
	}

}
