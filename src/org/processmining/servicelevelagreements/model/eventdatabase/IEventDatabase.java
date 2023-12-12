package org.processmining.servicelevelagreements.model.eventdatabase;

import java.util.Map;

import org.processmining.servicelevelagreements.model.interval.Interval;
import org.processmining.servicelevelagreements.model.interval.IntervalList;

import com.google.common.collect.Table;

/**
 * Interface to be used by event databases.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public interface IEventDatabase {

	public Map<String, Table<String, String, IntervalList>> getFluentValueMVIs();

	public void setFluentValueMVIs(Map<String, Table<String, String, IntervalList>> fluentValueMVIs);

	public boolean holdsAt(String fluent, String arguments, String value, long timestamp);

	public String holdsAt(String fluent, String arguments, long timestamp);

	public IntervalList holdsFor(String fluent, String arguments, String value);

	public boolean holdsFor(String fluent, String arguments, String value, Interval interval);

	public void initiate(String fluent, String arguments, String value, long timestamp);

	public void terminate(String fluent, String arguments, String value, long timestamp);

	public void set(String fluent, String arguments, String value, IntervalList list);

	public long getLastTimeStamp();

}
