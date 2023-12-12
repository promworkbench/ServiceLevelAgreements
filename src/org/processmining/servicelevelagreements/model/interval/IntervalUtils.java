package org.processmining.servicelevelagreements.model.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntervalUtils {
	static Pattern intervalPattern = Pattern.compile("^\\( *(\\d+|-) *, *(\\d+|inf|-) *\\)$");
	static Pattern intervalListPattern = Pattern.compile("(\\( *(\\d+|-) *, *(\\d+|inf|-) *\\))");

	/**
	 * Comparator that sorts Intervals based on their start timestamps.
	 * 
	 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
	 *
	 */
	public static class IntervalStartTimestampComparator implements Comparator<Interval> {
		public int compare(Interval i1, Interval i2) {
			return Long.compare(i1.getStartTimestamp(), i2.getStartTimestamp());
		}
	}

	/**
	 * Comparator that sorts Intervals based on their end timestamps. In case
	 * one of the intervals is open-ended, it is sorted last. In case both
	 * intervals are open-ended, they are considered to be equal.
	 * 
	 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
	 *
	 */
	public static class IntervalEndTimestampComparator implements Comparator<Interval> {
		public int compare(Interval i1, Interval i2) {
			if (i1.isOpenEnded() && !i2.isOpenEnded())
				return 1;
			else if (i2.isOpenEnded() && !i1.isOpenEnded())
				return 1;
			else if (i1.isOpenEnded() && i2.isOpenEnded())
				return 0;
			return Long.compare(i1.getEndTimestamp(), i2.getEndTimestamp());
		}
	}

	/**
	 * Comparator that sorts Intervals based on their start timestamps, and then
	 * on their end timestamps ({@link IntervalEndTimestampComparator}).
	 * 
	 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
	 *
	 */
	public static class IntervalStartAndEndTimestampComparator implements Comparator<Interval> {
		Comparator<Interval> intervalStartTimestampComparator = new IntervalStartTimestampComparator();
		Comparator<Interval> intervalEndTimestampComparator = new IntervalEndTimestampComparator();

		public int compare(Interval i1, Interval i2) {
			int compareBasedOnStartTimestamp = intervalStartTimestampComparator.compare(i1, i2);
			if (compareBasedOnStartTimestamp == 0)
				return intervalEndTimestampComparator.compare(i1, i2);
			else
				return compareBasedOnStartTimestamp;
		}
	}

	/**
	 * Computes the list I produced by removing empty intervals from the list L.
	 * 
	 * @param L
	 * @return I
	 */
	protected static IntervalList remove_empty_intervals(IntervalList L) {
		IntervalList I = new IntervalList(L);
		for (int i = 0; i < L.size(); i++) {
			if (L.get(i).isEmpty())
				I.remove(L.get(i));
		}
		return I;
	}

	/**
	 * Computes the list I of maximal intervals produced by the union of the
	 * lists of maximal intervals of list L.
	 * 
	 * L is allowed to be empty, contain empty lists and contain empty intervals
	 * in any of the lists.
	 * 
	 * @param L
	 * @return I
	 */
	public static IntervalList union_all(List<IntervalList> L) {
		IntervalList I = new IntervalList();
		if (L.isEmpty())
			return I;

		// Simplify L to a single list of intervals
		IntervalList sortedList = new IntervalList();
		for (IntervalList l : L) {
			sortedList.addAll(l);
		}

		// Sort L based on the start timestamps
		// sortedList.sort(new IntervalStartTimestampComparator()); // (JDK 8)
		Collections.sort(sortedList, new IntervalStartTimestampComparator());

		// Single walk through the sorted list to see which elements should be merged.
		Interval temp = new Interval(sortedList.get(0));
		for (int i = 1; i < sortedList.size(); i++) {
			if (sortedList.get(i).getStartTimestamp() > temp.getEndTimestamp()) {
				if (!temp.isEmpty())
					I.add(temp);
				temp = new Interval(sortedList.get(i).getStartTimestamp(), sortedList.get(i).getEndTimestamp());
			} else {
				if (sortedList.get(i).getEndTimestamp() > temp.getEndTimestamp()) {
					temp.setEndTimestamp(sortedList.get(i).getEndTimestamp());
				}
			}
		}
		if (!temp.isEmpty())
			I.add(temp);

		return remove_empty_intervals(I);
	}

	/**
	 * Computes the list of maximal intervals I such that I is the intersection
	 * of the lists of intervals of list L.
	 * 
	 * L is allowed to be empty, contain empty lists, or contain empty intervals
	 * in any of the lists.
	 * 
	 * @param L
	 * @return I
	 */
	public static IntervalList intersect_all(List<IntervalList> L) {
		IntervalList I = new IntervalList();
		if (L.isEmpty() || L.size() == 1)
			return I;

		I = intersect(L.get(0), L.subList(1, L.size()));

		return remove_empty_intervals(I);
	}

	/**
	 * Computes the list of maximal intervals I such that I is the intersection
	 * between the head and every element of the tail.
	 * 
	 * Head and tail are allowed to be empty or contain empty intervals.
	 * 
	 * @param head
	 * @param tail
	 * @return I
	 */
	protected static IntervalList intersect(IntervalList head, List<IntervalList> tail) {
		IntervalList I = new IntervalList(head);

		for (IntervalList tailElement : tail) {
			I = intersect(I, tailElement);
		}

		return I;
	}

	/**
	 * Computes the list of maximal intervals I such that I is the intersection
	 * between the two lists of intervals L1 and L2.
	 * 
	 * L1 and L2 are allowed to be empty or contain empty intervals.
	 * 
	 * @param L1
	 * @param L2
	 * @return I
	 */
	protected static IntervalList intersect(IntervalList L1, IntervalList L2) {
		IntervalList I = new IntervalList();

		for (Interval i : L1) {
			for (Interval j : L2) {
				I.add(intersect(i, j));
			}
		}

		return I;
	}

	/**
	 * Computes the interval x representing the intersection between two
	 * intervals i and j.
	 * 
	 * The intervals i and j are allowed to be empty or open-ended, but need to
	 * be closed-open intervals.
	 * 
	 * @param i
	 * @param j
	 * @return x
	 */
	protected static Interval intersect(Interval i, Interval j) {
		// i or j are empty
		if (i.isEmpty() || j.isEmpty())
			return new Interval();

		// i and j are open ended
		if (i.isOpenEnded() && j.isOpenEnded())
			if (i.getStartTimestamp() < j.getStartTimestamp())
				return new Interval(j);
			else
				return new Interval(i);

		// i == j
		if (i.equals(j))
			return new Interval(i);

		// i is fully contained in j
		if (i.getStartTimestamp() > j.getStartTimestamp() && i.getEndTimestamp() < j.getEndTimestamp())
			return new Interval(i);
		// j is fully contained in i
		if (j.getStartTimestamp() > i.getStartTimestamp() && j.getEndTimestamp() < i.getEndTimestamp())
			return new Interval(j);

		// overlap, i starts before j but ends after j started
		if (((i.getStartTimestamp() < j.getStartTimestamp() && i.getEndTimestamp() < j.getEndTimestamp())
				|| (i.getStartTimestamp() < j.getStartTimestamp() && j.isOpenEnded()))
				&& i.getEndTimestamp() > j.getStartTimestamp())
			return new Interval(j.getStartTimestamp(), i.getEndTimestamp());
		// overlap, j starts before i but ends after i started
		if (((j.getStartTimestamp() < i.getStartTimestamp() && j.getEndTimestamp() < i.getEndTimestamp())
				|| (j.getStartTimestamp() < i.getStartTimestamp() && i.isOpenEnded()))
				&& j.getEndTimestamp() > i.getStartTimestamp())
			return new Interval(i.getStartTimestamp(), j.getEndTimestamp());

		return new Interval();
	}

	/**
	 * Computes the list of maximal intervals I such that I is the relative
	 * complement of the list of maximal intervals J with respect to the maximal
	 * intervals of list L.
	 * 
	 * @param Iprime
	 * @param L
	 * @return I
	 */
	public static IntervalList relative_complement_all(IntervalList Iprime, List<IntervalList> L) {
		if (L.isEmpty())
			return new IntervalList(Iprime);

		// Create the union_all of L
		IntervalList L_union_all = union_all(L);

		// Create the intersection between J and Lunion
		IntervalList intersection = intersect(Iprime, L_union_all);

		// I = J minus the intersection
		IntervalList I = minus(remove_empty_intervals(Iprime), remove_empty_intervals(intersection));

		return remove_empty_intervals(I);
	}

	/**
	 * Computes the list of maximal intervals M such that M = I / J.
	 * 
	 * J is assumed not to have any overlapping intervals (since the origin is
	 * usually the union of a {@code List<<IntervalList>}.
	 * 
	 * @param I
	 * @param J
	 * @return M
	 */
	protected static IntervalList minus(IntervalList I, IntervalList J) {
		if (J.isEmpty())
			return new IntervalList(I);

		// Create local copies
		IntervalList localI = new IntervalList(I);
		IntervalList localJ = new IntervalList(J);

		// Sort local I
		//		localI.sort(new IntervalStartAndEndTimestampComparator()); // (JDK 8)
		Collections.sort(localI, new IntervalStartAndEndTimestampComparator());

		// Sort local J
		//		localJ.sort(new IntervalStartAndEndTimestampComparator()); // (JDK 8)
		Collections.sort(localJ, new IntervalStartAndEndTimestampComparator());

		// Compute the difference
		IntervalList M = new IntervalList();
		Iterator<Interval> jit = localJ.iterator();
		Interval j = jit.next();
		i: for (Interval i : localI) {
			if (i.isEmpty())
				continue i;
			if (j.getStartTimestamp() > i.getEndTimestamp()) {
				// because localJ is sorted, there's no j in the future that will affect this i
				M.add(new Interval(i));
				continue i;
			}
			while (j.getEndTimestamp() <= i.getStartTimestamp())
				// this j ended before this i started, so take the next j
				j = jit.next();

			// At this point there's either overlap or complete incapsulation
			if (i.getStartTimestamp() < j.getStartTimestamp()) {
				// i starts before j starts
				if (i.getEndTimestamp() <= j.getEndTimestamp()) {
					// i ends before (or ends at the same time as) j ends (one segment)
					M.add(new Interval(i.getStartTimestamp(), j.getStartTimestamp()));
					continue i;
				} else {
					// i ends after j ends

					// first add the first segment
					M.add(new Interval(i.getStartTimestamp(), j.getStartTimestamp()));

					if (j.isOpenEnded())
						// this j ends it all
						break;

					// if j ends at some point, add the remaining time in i until the next j starts, and keep doing so until this i is handled
					do {
						long start = j.getEndTimestamp();
						if (jit.hasNext()) {
							j = jit.next();
							M.add(new Interval(start, j.getStartTimestamp()));
						} else {
							M.add(new Interval(start, i.getEndTimestamp()));
							break;
						}
					} while (j.getStartTimestamp() < i.getEndTimestamp());

					continue i;
				}
			} else {
				// j starts before i starts

				if (j.isOpenEnded())
					// this j ends it all
					break i;

				if (i.getEndTimestamp() > j.getEndTimestamp()) {
					// i ends after j ends
					do {
						long start = j.getEndTimestamp();
						if (jit.hasNext()) {
							j = jit.next();
							M.add(new Interval(start, j.getStartTimestamp()));
						} else {
							M.add(new Interval(start, i.getEndTimestamp()));
							break;
						}
					} while (j.getStartTimestamp() < i.getEndTimestamp());
				} else {
					// i is enclosed by this j so we disregard this i and continue with the next i
					continue i;
				}

			}

		}

		return M;
	}

	public static Interval textToInterval(String text) {
		Matcher matcher = intervalPattern.matcher(text);

		if (matcher.find()) {
			if (matcher.group(1).equals("-"))
				return new Interval();
			else if (matcher.group(2).equals("inf"))
				return new Interval(Long.parseLong(matcher.group(1)));
			else
				return new Interval(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)));
		}

		return null;
	}

	public static IntervalList textToIntervalList(String text) {
		Matcher matcher = intervalListPattern.matcher(text);

		if (matcher.groupCount() == 0)
			return null;

		IntervalList list = new IntervalList();
		while (matcher.find()) {
			list.add(textToInterval(matcher.group()));
		}

		return list;
	}

	public static List<IntervalList> textToListOfIntervalList(String text) {
		/*
		 * Since regular expressions cannot capture a variable number of
		 * non-capturing groups, first split on the list separators, then pass
		 * each element through.
		 */
		if (text.length() <= 2)
			return null;

		// Remove [ and ] around list of lists
		text = text.substring(1, text.length() - 1);
		// Split into separate lists
		String[] lists = text.split("\\] *, *\\[");

		List<IntervalList> listOfLists = new ArrayList<IntervalList>();
		for (int i = 0; i < lists.length; i++) {
			listOfLists.add(textToIntervalList(lists[i]));
		}

		return listOfLists;
	}

}
