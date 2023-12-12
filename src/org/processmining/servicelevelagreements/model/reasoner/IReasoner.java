package org.processmining.servicelevelagreements.model.reasoner;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;

/**
 * Interface to be used by reasoners.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public interface IReasoner {

	public EventDatabase getDatabase();

	public void setDatabase(EventDatabase database);

	public void handleAgreement(ServiceLevelAgreement agreement);

	public void handleEvent(XEvent xevent);

	public void createMappings(XLog xlog, ServiceLevelAgreementsParameters parameters);

}
