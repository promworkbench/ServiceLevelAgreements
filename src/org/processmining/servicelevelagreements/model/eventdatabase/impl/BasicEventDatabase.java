package org.processmining.servicelevelagreements.model.eventdatabase.impl;

import java.util.HashMap;

import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.interval.IntervalList;

import com.google.common.collect.Table;

/**
 * Basic Event database. All sets, maps, and tables are hash-based.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class BasicEventDatabase extends EventDatabase {

	// CONSTRUCTORS

	public BasicEventDatabase() {
		setFluentValueMVIs(new HashMap<String, Table<String, String, IntervalList>>());
	}

}
