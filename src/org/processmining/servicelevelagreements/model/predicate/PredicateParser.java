package org.processmining.servicelevelagreements.model.predicate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicateParser {

	static Pattern initiatedAtPattern = Pattern
			.compile("^initiatedAt\\(\\s*(\\w+)\\((.*)\\)\\s*=\\s*(\\w+)\\s*,\\s*(\\w+)\\s*\\) *:?$");
	static Pattern terminatedAtPattern = Pattern
			.compile("^terminatedAt\\(\\s*(\\w+)\\((.*)\\)\\s*=\\s*(\\w+)\\s*,\\s*(\\w+)\\s*\\) *:?$");
	//	static Pattern happensAtPattern = Pattern.compile("^happensAt\\((.*)\\s*,\\s*(\\w+)\\) *,?$");
	static Pattern happensAtPattern = Pattern.compile("^happensAt\\((.*)\\((.*)\\)\\s*,\\s*(\\w+)\\) *,?$");
	static Pattern holdsAtPattern = Pattern
			.compile("^!?holdsAt\\(\\s*(\\w+)\\((.*)\\)\\s*=\\s*(\\w+)\\s*,\\s*(\\w+)\\s*\\) *,?$");
	static Pattern holdsForPattern = Pattern
			.compile("^!?holdsFor\\(\\s*(\\w+)\\((.*)\\)\\s*=\\s*(\\w+)\\s*,\\s*(\\w+)\\s*\\) *:?,?$");
	static Pattern relativeComplementAllPattern = Pattern
			.compile("^!?relative_complement_all\\((\\w+)\\s*,(.*)\\s*,\\s*(\\w+)\\) *,?$");
	static Pattern unionAllPattern = Pattern.compile("^!?union_all\\((.*)\\s*,\\s*(\\w+)\\) *,?$");
	static Pattern intersectAllPattern = Pattern.compile("^!?intersect_all\\((.*)\\s*,\\s*(\\w+)\\) *,?$");

	/**
	 * Parses a String containing a initiatedAt(F(A)=V,T) RTEC predicate.
	 * 
	 * @param predicate
	 *            The initiatedAt RTEC line initiatedAt(F(A)=V,T)
	 * @return new String[]{F,A,V,T}
	 */
	public static String[] parseInitiatedAt(String predicate) {

		Matcher matcher = initiatedAtPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };
		}

		return null;
	}

	/**
	 * Parses a String containing a terminatedAt(F(A)=V,T) RTEC predicate.
	 * 
	 * @param predicate
	 *            The terminatedAt RTEC line terminatedAt(F(A)=V,T)
	 * @return new String[]{F,A,V,T}
	 */
	public static String[] parseTerminatedAt(String predicate) {

		Matcher matcher = terminatedAtPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };
		}

		return null;
	}

	/**
	 * Parses a String containing a happensAt(ET(A),T) RTEC predicate, where ET
	 * is the event type and A are arguments of ET.
	 * 
	 * TODO [low] handle empty A in parsing regex.
	 * 
	 * @param predicate
	 *            The happensAt RTEC line happensAt(ET(A),T)
	 * @return new String[]{ET(A),T}
	 */
	public static String[] parseHappensAt(String predicate) {

		Matcher matcher = happensAtPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1) + "(" + matcher.group(2) + ")", matcher.group(3) };
		}

		return null;
	}

	/**
	 * Parses a String containing a holdsAt(F(A)=V,T) RTEC predicate.
	 * 
	 * @param predicate
	 *            The happensAt RTEC line holdsAt(F(A)=V,T)
	 * @return new String[]{F,A,V,T}
	 */
	public static String[] parseHoldsAt(String predicate) {

		Matcher matcher = holdsAtPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };
		}

		return null;
	}

	/**
	 * Parses a String containing a holdsFor(F(A)=V, I) RTEC predicate.
	 * 
	 * TODO [medium] make it possible to parse I into an interval object
	 * 
	 * @param predicate
	 *            The holdsFor RTEC line holdsFor(F(A)=V,I)
	 * @return new String[]{F,A,V,I}
	 */
	public static String[] parseHoldsFor(String predicate) {

		Matcher matcher = holdsForPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };
		}

		return null;
	}

	/**
	 * Parses a String containing a relative_complement_all(I',L,I) RTEC
	 * predicate.
	 * 
	 * @param predicate
	 *            The relative_complement_all RTEC line
	 *            relative_complement_all(I',L,I)
	 * @return new String[]{I',L,I}
	 */
	public static String[] parseRelativeComplementAll(String predicate) {

		Matcher matcher = relativeComplementAllPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3) };
		}

		return null;
	}

	/**
	 * Parses a String containing a union_all(L,I) RTEC predicate.
	 * 
	 * @param predicate
	 *            The union_all RTEC line union_all(L,I)
	 * @return new String[]{L,I}
	 */
	public static String[] parseUnionAll(String predicate) {

		Matcher matcher = unionAllPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2) };
		}

		return null;
	}

	/**
	 * Parses a String containing a intersect_all(L,I) RTEC predicate.
	 * 
	 * @param predicate
	 *            The union_all RTEC line intersect_all(L,I)
	 * @return new String[]{L,I}
	 */
	public static String[] parseIntersectAll(String predicate) {

		Matcher matcher = intersectAllPattern.matcher(predicate);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2) };
		}

		return null;
	}

	/**
	 * Parses a String containing a list of lists L=[A,B,C,...].
	 * 
	 * @param predicate
	 *            The list of lists L=[A,B,C,...]
	 * @return new String[]{A,B,C,...}
	 */
	public static String[] parseListofLists(String predicate) {
		if (predicate.length() <= 2)
			return null;
		return predicate.trim().substring(1, predicate.length() - 1).split("\\s*,\\s*");
	}
}
