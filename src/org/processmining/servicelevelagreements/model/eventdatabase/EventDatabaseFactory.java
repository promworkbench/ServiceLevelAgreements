package org.processmining.servicelevelagreements.model.eventdatabase;

import org.processmining.servicelevelagreements.model.eventdatabase.impl.BasicEventDatabase;

/**
 * Factory for event databases.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class EventDatabaseFactory {

	public static EventDatabase getEventDatabase(EventDatabaseType type) {
		switch (type) {
			case BASIC :
				return new BasicEventDatabase();
			default :
				return null;
		}
	}

}
