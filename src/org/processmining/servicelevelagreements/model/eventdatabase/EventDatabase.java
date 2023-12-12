package org.processmining.servicelevelagreements.model.eventdatabase;

import java.util.Map;

import org.processmining.servicelevelagreements.model.interval.Interval;
import org.processmining.servicelevelagreements.model.interval.IntervalList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Abstract class to keep track of states of fluents, intervals, etc.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public abstract class EventDatabase implements IEventDatabase {

	// FIELDS
	
	private long lastTimestamp;

	// Mapping from String (fluent including arguments) to list of values to MVIs for the fluent(argument), value combination.
	private Map<String, Table<String, String, IntervalList>> fluentValueMVIs;

	// CONSTRUCTORS

	public EventDatabase() {

	}

	// GETTERS AND SETTERS

	public Map<String, Table<String, String, IntervalList>> getFluentValueMVIs() {
		return fluentValueMVIs;
	}

	public void setFluentValueMVIs(Map<String, Table<String, String, IntervalList>> fluentMVIs) {
		this.fluentValueMVIs = fluentMVIs;
	}

	// METHODS

	/**
	 * Queries the database to see whether holdsAt(F(A)=V,T) holds.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @param timestamp
	 *            T
	 * @return
	 */
	public boolean holdsAt(String fluent, String arguments, String value, long timestamp) {
		if (!fluentValueMVIs.containsKey(fluent))
			return false;
		if (!fluentValueMVIs.get(fluent).contains(arguments, value))
			return false;

		IntervalList L = fluentValueMVIs.get(fluent).get(arguments, value);

		return L.containsTimestamp(timestamp);
	}

	/**
	 * Queries the database to see what value V has at timestamp T in
	 * holdsAt(F(A)=V,T). In case no value exists, null is returned.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param timestamp
	 *            T
	 * @return value V
	 */
	public String holdsAt(String fluent, String arguments, long timestamp) {
		if (!fluentValueMVIs.containsKey(fluent))
			return null;
		if (!fluentValueMVIs.get(fluent).containsRow(arguments))
			return null;

		Map<String, IntervalList> m = fluentValueMVIs.get(fluent).row(arguments);

		for (String value : m.keySet()) {
			if (m.get(value).containsTimestamp(timestamp))
				return value;
		}

		return null;
	}

	/**
	 * Queries the database for holdsFor(F(A, I) and returns I.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @return I
	 */
	public IntervalList holdsFor(String fluent, String arguments, String value) {
		if (!fluentValueMVIs.containsKey(fluent))
			return new IntervalList();
		if (!fluentValueMVIs.get(fluent).contains(arguments, value))
			return new IntervalList();
		return fluentValueMVIs.get(fluent).get(arguments, value);
	}

	/**
	 * Queries the database for holdsFor(F(A),I) and returns whether it holds.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @param interval
	 *            I
	 * @return
	 */
	public boolean holdsFor(String fluent, String arguments, String value, Interval interval) {
		if (!fluentValueMVIs.containsKey(fluent))
			return false;
		if (!fluentValueMVIs.get(fluent).contains(arguments, value))
			return false;

		IntervalList L = fluentValueMVIs.get(fluent).get(arguments, value);

		return L.containsInterval(interval);
	}

	/**
	 * Will initiate the value V for the fluent F(A) at timestamp T. Will also
	 * terminate all other values for F(A) at timestamp T since fluents can have
	 * only one value at any given time.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @param timestamp
	 *            T
	 */
	public void initiate(String fluent, String arguments, String value, long timestamp) {
		System.out.println("- Initializing '" + fluent + "(" + arguments + ") = " + value + " @ " + timestamp + "'");

		// If the fluent already has the value at the timestamp, do nothing.
		if (holdsAt(fluent, arguments, value, timestamp)) {
			System.out.println("- Value already holds at timestamp.");
			return;
		}

		// If the fluent does not have the value at the timestamp, initiate it.
		// First, terminate all other values for this fluent
		if (fluentValueMVIs.containsKey(fluent)) {
			for (String otherValue : fluentValueMVIs.get(fluent).row(arguments).keySet()) {
				if (!otherValue.equals(value))
					terminate(fluent, arguments, otherValue, timestamp);
			}
		}

		// Then, initiate the fluent value
		if (!fluentValueMVIs.containsKey(fluent)) {
			Table<String, String, IntervalList> t = HashBasedTable.create();
			fluentValueMVIs.put(fluent, t);
		}
		if (!fluentValueMVIs.get(fluent).contains(arguments, value))
			fluentValueMVIs.get(fluent).put(arguments, value, new IntervalList());
		fluentValueMVIs.get(fluent).get(arguments, value).start(timestamp);

		System.out.println("- Initialized '" + fluent + "(" + arguments + ") = " + value + " @ " + timestamp + "'");
	}

	/**
	 * Will terminate the value V for the fluent F(A) at timestamp T.
	 * 
	 * @param fluent
	 *            FF
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @param timestamp
	 *            T
	 */
	public void terminate(String fluent, String arguments, String value, long timestamp) {
		// If the fluent already does not has the value at the timestamp, do nothing.
		if (!holdsAt(fluent, arguments, value, timestamp))
			return;

		System.out.println("- Terminating '" + fluent + "(" + arguments + ") = " + value + " @ " + timestamp + "'");

		if (fluentValueMVIs.containsKey(fluent))
			if (fluentValueMVIs.get(fluent).contains(arguments, value))
				fluentValueMVIs.get(fluent).get(arguments, value).end(timestamp);

		System.out.println("- Terminated '" + fluent + "(" + arguments + ") = " + value + " @ " + timestamp + "'");
	}

	/**
	 * Will set the fluent F(A) to value V for the list of intervals L.
	 * 
	 * @param fluent
	 *            F
	 * @param arguments
	 *            A
	 * @param value
	 *            V
	 * @param list
	 *            L
	 */
	public void set(String fluent, String arguments, String value, IntervalList list) {
		System.out.println("- Setting '" + fluent + "(" + arguments + ") = " + value + " @ " + list.toString() + "'");

		if (!fluentValueMVIs.containsKey(fluent)) {
			Table<String, String, IntervalList> t = HashBasedTable.create();
			fluentValueMVIs.put(fluent, t);
		}
		fluentValueMVIs.get(fluent).put(arguments, value, list);

		System.out.println("- Set '" + fluent + "(" + arguments + ") = " + value + " @ " + list.toString() + "'");
	}

	public long getLastTimeStamp() {
		return lastTimestamp;
	}

	public void setLastTimeStamp(long timestamp) {
		this.lastTimestamp = timestamp;
	}

}
