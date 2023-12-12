package org.processmining.servicelevelagreements.algorithm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.servicelevelagreements.algorithm.ServiceLevelAgreementsAlgorithm;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.reasoner.ReasonerFactory;
import org.processmining.servicelevelagreements.model.reasoner.ReasonerType;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.xes.comparators.XEventTimeStampComparator;
import org.processmining.servicelevelagreements.model.xes.extensions.XCaseExtension;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;

/**
 * This algorithm takes as input an event log in XLog format and outputs a
 * constraint log and optionally an enrichted event log in XLog format.
 * 
 * The Basic SLA algorithm uses the Basic Reasoner to reason about SLAs.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class BasicServiceLevelAgreementsAlgorithm extends ServiceLevelAgreementsAlgorithm {

	private ArrayList<XEvent> events;

	// CONSTRUCTORS

	public BasicServiceLevelAgreementsAlgorithm() {
		setReasoner(ReasonerFactory.getReasoner(ReasonerType.BASIC_DEDUCTIVE));
	}

	// METHODS

	/**
	 * The actual (basic) SLA algorithm.
	 * 
	 * @param xlog
	 * @param parameters
	 * @return
	 */
	protected EventDatabase apply(XLog xlog, ServiceLevelAgreementsParameters parameters) {
		int nrCases = 0;
		int nrEvents = 0;
		events = new ArrayList<XEvent>();
		setReasoner(ReasonerFactory.getReasoner(ReasonerType.BASIC_DEDUCTIVE));

		// Initialize the reasoner
		reasoner.createMappings(xlog, parameters);

		// Pass the SLA constraints to the reasoner to store the effects of events and fluents.
		for (ServiceLevelAgreement agreement : parameters.getServiceLevelAgreements()) {
			reasoner.handleAgreement(agreement);
		}

		// Loop over the traces in the log and add them to one list that can be sorted.
		for (XTrace xtrace : xlog) {

			for (int e = 0; e < xtrace.size(); e++) {

				XEvent xevent = xtrace.get(e);

				if (e == 0) {
					// Add an artificial start event before the first event of the trace.
					XEvent startEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(startEvent, "artificialStartEvent");
					XConceptExtension.instance().assignInstance(startEvent, "artificialStartEvent" + nrCases);
					XLifecycleExtension.instance().assignStandardTransition(startEvent, StandardModel.COMPLETE);
					XOrganizationalExtension.instance().assignResource(startEvent, "sla-system");
					XTimeExtension.instance().assignTimestamp(startEvent,
							XTimeExtension.instance().extractTimestamp(xevent));
					XCaseExtension.instance().assignCase(startEvent, xtrace);
					events.add(startEvent);
					//reasoner.handleEvent(startEvent);					
					nrEvents++;
				}

				// Pass this event to the reasoner to update (or not) the event database
				XCaseExtension.instance().assignCase(xevent, xtrace);
				events.add(xevent);
				//reasoner.handleEvent(xevent);
				nrEvents++;

				if (e == xtrace.size() - 1) {
					// Add an artificial end event after the last event of the trace.
					XEvent endEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(endEvent, "artificialEndEvent");
					XConceptExtension.instance().assignInstance(endEvent, "artificialEndEvent" + nrCases);
					XLifecycleExtension.instance().assignStandardTransition(endEvent, StandardModel.COMPLETE);
					XOrganizationalExtension.instance().assignResource(endEvent, "sla-system");
					XTimeExtension.instance().assignTimestamp(endEvent,
							XTimeExtension.instance().extractTimestamp(xevent));
					XCaseExtension.instance().assignCase(endEvent, xtrace);
					events.add(endEvent);
					//reasoner.handleEvent(endEvent);
					nrEvents++;
				}
			}

			nrCases++;
		}

		// Sort the events based on their timestamp
		Collections.sort(events, new XEventTimeStampComparator());

		// Pass the events to the reasoner
		for (XEvent event : events) {
			reasoner.handleEvent(event);
		}

		// Set the last event timestamp in the event log
		reasoner.getDatabase()
				.setLastTimeStamp(XTimeExtension.instance().extractTimestamp(events.get(events.size() - 1)).getTime());

		// Print a statement
		System.out.println("\nHandled " + reasoner.getHandledSDEs() + " out of " + nrEvents + " events ("
				+ 100.0 / nrEvents * Math.round(100 * reasoner.getHandledSDEs()) / 100.0
				+ " %) (including artificial start and end events).\n");

		// Get the database and remove the internal fluents not related to named constraints.
		EventDatabase db = reasoner.getDatabase();
		Set<String> namedConstraints = new HashSet<String>();
		for (ServiceLevelAgreement agreement : parameters.getServiceLevelAgreements()) {
			namedConstraints.add(agreement.getName());
		}
		for (Iterator<String> it = db.getFluentValueMVIs().keySet().iterator(); it.hasNext();) {
			String fluent = it.next();
			if (!namedConstraints.contains(fluent))
				it.remove();
		}

		// Return the db
		return db;
	}

}
