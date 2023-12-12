package org.processmining.servicelevelagreements.model.reasoner;

import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;

/**
 * Abstract class that reasons over the provided SLAs.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public abstract class Reasoner implements IReasoner {

	// FIELDS

	private EventDatabase database;
	private int handledSDEs;

	// CONSTRUCTORS

	public Reasoner() {

	}

	public Reasoner(EventDatabase database) {
		setDatabase(database);
		setHandledSDEs(0);
	}

	// GETTERS AND SETTERS

	public EventDatabase getDatabase() {
		return database;
	}

	public void setDatabase(EventDatabase database) {
		this.database = database;
	}

	public int getHandledSDEs() {
		return handledSDEs;
	}

	public void setHandledSDEs(int handledSDEs) {
		this.handledSDEs = handledSDEs;
	}

}
