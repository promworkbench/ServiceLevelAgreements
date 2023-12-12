package org.processmining.servicelevelagreements.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomTest {

	public static void main(String[] args) {
		int i = 1;

		while (i < 3) {
			i++;
			System.out.println(3 - i);
		}

		i = 5;
		while (i >= 0) {
			if (false && (1 + 2 > i))
				System.out.println("ERROR");
			else
				System.out.println("YES");
			i--;
		}

		System.out.println(extractBindingVariablesFromText("_CaseConceptName"));

	}

	private static List<String> extractBindingVariablesFromText(String text) {
		Pattern pattern = Pattern.compile("(\\b\\w+\\b)");
		Matcher matcher = pattern.matcher(text);
		//		if (!matcher.matches())
		//			return new ArrayList<String>();
		List<String> elements = new ArrayList<String>(matcher.groupCount());
		while (matcher.find()) {
			elements.add(matcher.group(0));
		}
		return elements;
	}
}
