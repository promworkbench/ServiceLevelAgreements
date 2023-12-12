package org.processmining.servicelevelagreements.model.interval;

import java.util.ArrayList;

public class IntervalList extends ArrayList<Interval> {

	// FIELDS

	private static final long serialVersionUID = -569823316996260155L;

	// CONSTRUCTORS

	public IntervalList() {
		super();
	}

	public IntervalList(int initialCapacity) {
		super(initialCapacity);
	}

	public IntervalList(IntervalList other) {
		super(other);
	}

	// METHODS

	/**
	 * Checks whether this list contains any interval that contains the
	 * specified timestamp. The list is traversed in reverse order.
	 * 
	 * @param timestamp
	 *            The timestamp
	 * @return True if the list contains an interval that contains the
	 *         timestamp, false otherwise.
	 */
	public boolean containsTimestamp(long timestamp) {

		for (int i = size() - 1; i >= 0; i--) {
			if (get(i).containsTimestamp(timestamp))
				return true;
			if (get(i).getEndTimestamp() <= timestamp)
				break;
		}

		return false;
	}

	/**
	 * Checks whether this list contains the specified interval. The list is
	 * traversed in reverse order.
	 * 
	 * @param interval
	 *            The interval
	 * @return True if the list contains an interval that fully contains the
	 *         specified interval, false otherwise.
	 */
	public boolean containsInterval(Interval interval) {

		for (int i = size() - 1; i >= 0; i--) {

			if (get(i).containsInterval(interval))
				return true;
		}

		return false;
	}

	/**
	 * Will try to change the end timestamp of the last interval in the list to
	 * the provided timestamp T. When the last interval is empty the previous
	 * interval will be tried etc.
	 * 
	 * When an interval is reached that has an end timestamp that is greater
	 * than T, a notification is thrown.
	 * 
	 * @param timestamp
	 *            T
	 */
	public void end(long timestamp) {
		if (size() < 1)
			return;

		int i = 1;
		Interval interval = get(size() - i);
		while (interval.isEmpty() && i < size()) {
			i++;
			interval = get(size() - i);
		}

		if (interval.isEmpty())
			return;

		//TODO [medium] In case a closed interval is adjusted to the past, it might mean some values used in the past were incorrect. This happens when events occurred before events that have already been handled. Need to update or revise the conclusions.
		if (!interval.isOpenEnded() && interval.getEndTimestamp() > timestamp)
			System.out.println("WARNING - trying to end an interval with a date that is in the past!");

		interval.setEndTimestamp(timestamp);
	}

	/**
	 * Will add a new open-ended interval (T,-) to the end of the list.
	 * 
	 * @param timestamp
	 *            T
	 */
	public void start(long timestamp) {
		// This method should only be called when holdsAt(F(A)=V,T) is false, so add a new open-ended interval from timestamp.
		this.add(new Interval(timestamp));
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
