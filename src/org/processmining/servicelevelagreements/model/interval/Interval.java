package org.processmining.servicelevelagreements.model.interval;

import com.google.common.base.Objects;

/**
 * 
 * Represents the, possibly empty, possibly open-ended (end timestamp is
 * infinite), closed-open interval {@code [startTimestamp, endTimestamp)}.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class Interval {

	private long startTimestamp;
	private long endTimestamp;
	private boolean empty;
	private boolean openEnded;

	/**
	 * Constructor for empty interval.
	 */
	public Interval() {
		this.setEmpty(true);
	}

	/**
	 * Constructor for interval with open end.
	 * 
	 * @param startTimestamp
	 */
	public Interval(long startTimestamp) {
		this.setStartTimestamp(startTimestamp);
		this.setEmpty(false);
		this.setOpenEnded(true);
	}

	/**
	 * Constructor for interval with closed end.
	 * 
	 * @param startTimestamp
	 * @param endTimestamp
	 */
	public Interval(long startTimestamp, long endTimestamp) {
		this.setStartTimestamp(startTimestamp);
		this.setEndTimestamp(endTimestamp);
		this.setEmpty(false);
		this.setOpenEnded(false);
	}

	public Interval(Interval other) {
		this.setEmpty(other.isEmpty());
		if (!empty) {
			this.setStartTimestamp(other.getStartTimestamp());
			this.setOpenEnded(other.isOpenEnded());
		}
		if (!empty && !openEnded) {
			this.setEndTimestamp(other.getEndTimestamp());
		}
	}

	// GETTERS AND SETTERS
	public long getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(long timestamp) {
		this.setEmpty(false);
		this.startTimestamp = timestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(long timestamp) {
		this.setEmpty(false);
		this.setOpenEnded(false);
		this.endTimestamp = timestamp;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isOpenEnded() {
		return openEnded;
	}

	public void setOpenEnded(boolean openEnded) {
		this.openEnded = openEnded;
	}

	// METHODS

	/**
	 * Get's the length of the interval.
	 * 
	 * @return -1 in case the interval is open ended or the difference between
	 *         end and start timestamp in case the interval is closed.
	 */
	public long getLength() {
		return openEnded ? -1 : endTimestamp - startTimestamp;
	}

	/**
	 * Checks whether this interval contains the specified timestamp.
	 * 
	 * @param timestamp
	 *            The timestamp
	 * @return True if the timestamp is contained in the interval, false
	 *         otherwise.
	 */
	public boolean containsTimestamp(long timestamp) {

		// If this interval is empty, return false
		if (empty)
			return false;
		// If this interval starts after the timestamp, return false
		if (startTimestamp > timestamp)
			return false;
		// If this interval is open ended (and starts before or at the timestamp), return true
		if (openEnded)
			return true;
		// If this interval ends after the timestamp (and starts before or at the timestamp), return true
		if (endTimestamp > timestamp)
			return true;

		return false;

	}

	/**
	 * Checks whether this interval contains the specified interval.
	 * 
	 * @param other
	 *            The other interval
	 * @return True if the other interval is contained in this interval, false
	 *         otherwise.
	 */
	public boolean containsInterval(Interval other) {

		// If this interval is empty, return false
		if (empty)
			return false;

		// If the other interval is empty, return true 
		if (other.isEmpty())
			return false;

		// If this is open ended, only check start timestamps
		if (openEnded)
			if (startTimestamp <= other.getStartTimestamp())
				return true;
			else
				return false;

		// If other is open ended but this is not, return false
		if (other.isOpenEnded())
			return false;

		// Neither intervals are open ended
		// If other is contained by this, return true
		if (other.getStartTimestamp() >= startTimestamp && other.getEndTimestamp() <= endTimestamp)
			return true;

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Interval))
			return false;

		Interval other = (Interval) obj;

		if (other.isEmpty() != empty)
			return false;

		if (other.isOpenEnded() != openEnded)
			return false;

		if (other.getStartTimestamp() != startTimestamp)
			return false;

		if (!openEnded && other.getEndTimestamp() != endTimestamp)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(startTimestamp, endTimestamp, empty, openEnded);
	}

	@Override
	public String toString() {
		return empty ? "(-,-)" : "(" + startTimestamp + "," + (openEnded ? "inf" : endTimestamp) + ")";
	}

}
