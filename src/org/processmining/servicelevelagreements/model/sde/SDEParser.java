package org.processmining.servicelevelagreements.model.sde;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDEParser {

	static Pattern SDEPattern = Pattern.compile(
			"^(\\b\\w+\\b)\\(\\s*\\s*(\\b[\\w-]+\\b)\\s*,\\s*(\\b[\\w-]+\\b)\\s*,\\s*(\\b[\\w-]+\\b)\\s*,\\s*(\\b[\\w-]+\\b)\\)$");

	/**
	 * Parses a String containing an RTEC SDE of the form ET(A). The SDE is
	 * parsed as follows: ET = lifecycletransition A=(activityconceptname,
	 * activityinstanceid, caseconceptname, resource)
	 * 
	 * @param sde
	 *            The RTEC SDE event ET(A)
	 * @return new String[]{lifecycletransition, activityconceptname,
	 *         activityinstanceid, caseconceptname, resource}
	 */
	//TODO [low] maybe make return type SDE object rather than String array to increase readability at the cost of performance?
	public static String[] parseSDE(String sde) {

		Matcher matcher = SDEPattern.matcher(sde);

		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4),
					matcher.group(5) };
		}

		return null;
	}

}
