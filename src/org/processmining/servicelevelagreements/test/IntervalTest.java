package org.processmining.servicelevelagreements.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.servicelevelagreements.model.interval.Interval;
import org.processmining.servicelevelagreements.model.interval.IntervalList;
import org.processmining.servicelevelagreements.model.interval.IntervalUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class IntervalTest {

	public static void main(String[] args) {

		// BASE INTERVALS

		Interval empty = new Interval();
		Interval i520 = new Interval(5, 20);
		Interval i2630 = new Interval(26, 30);
		Interval i2835 = new Interval(28, 35);
		Interval i14 = new Interval(1, 4);
		Interval i2126 = new Interval(21, 26);
		Interval i3040 = new Interval(30, 40);
		Interval i2650 = new Interval(26, 50);
		Interval i1822 = new Interval(18, 22);
		Interval i6070 = new Interval(60, 70);
		Interval i5565 = new Interval(55, 65);
		Interval i5280 = new Interval(52, 80);

		// UNION_ALL

		IntervalList lu1 = new IntervalList();
		lu1.add(i520);
		lu1.add(i2630);
		lu1.add(empty);

		IntervalList lu2 = new IntervalList();
		lu2.add(i14);
		lu2.add(i2126);

		IntervalList lempty = new IntervalList();

		List<IntervalList> LU = new ArrayList<IntervalList>();
		LU.add(lu1);
		LU.add(lu2);
		LU.add(lempty);

		IntervalList IU1 = IntervalUtils.union_all(LU);

		System.out.println("empty interval: " + empty);
		System.out.println("interval (5,20): " + i520);
		System.out.println("list 1: " + lu1);
		System.out.println("list 2: " + lu2);
		System.out.println("empty list:" + lempty);
		System.out.println("union_all 1: " + IU1);

		// INTERSECT_ALL

		IntervalList li1 = new IntervalList();
		li1.add(i520);
		li1.add(i2630);

		IntervalList li2 = new IntervalList();
		li2.add(i2835);

		List<IntervalList> LI = new ArrayList<IntervalList>();
		LI.add(li1);
		LI.add(li2);

		IntervalList II1 = IntervalUtils.intersect_all(LI);

		IntervalList li3 = new IntervalList();
		li3.add(i520);
		li3.add(i2630);

		IntervalList li4 = new IntervalList();
		li4.add(i14);
		li4.add(i2126);
		li4.add(i3040);

		List<IntervalList> LI2 = new ArrayList<IntervalList>();
		LI2.add(li3);
		LI2.add(li4);

		IntervalList II2 = IntervalUtils.intersect_all(LI2);

		System.out.println("intersect_all 1: " + II1);
		System.out.println("intersect_all 2: " + II2);

		// RELATIVE_COMPLEMENT_ALL
		IntervalList lr1 = new IntervalList();
		lr1.add(i520);
		lr1.add(i2650);

		IntervalList lr2 = new IntervalList();
		lr2.add(i14);
		lr2.add(i1822);
		lr2.add(i2835);

		List<IntervalList> LR1 = new ArrayList<IntervalList>();
		LR1.add(lr2);

		IntervalList IR1 = IntervalUtils.relative_complement_all(lr1, LR1);

		IntervalList lr3 = new IntervalList();
		lr3.add(i520);
		lr3.add(i2650);
		lr3.add(i6070);

		IntervalList lr4 = new IntervalList();
		lr4.add(i14);
		lr4.add(i5565);

		IntervalList lr5 = new IntervalList();
		lr5.add(i5280);

		List<IntervalList> LR2 = new ArrayList<IntervalList>();
		LR2.add(lr4);
		LR2.add(lempty);
		LR2.add(lr5);

		IntervalList IR2 = IntervalUtils.relative_complement_all(lr3, LR2);

		IntervalList IR3 = IntervalUtils.relative_complement_all(lr1, new ArrayList<IntervalList>());

		System.out.println("relative_complement_all 1: " + IR1);
		System.out.println("relative_complement_all 2: " + IR2);
		System.out.println("relative_complement_all 3: " + IR3);

		String text;
		text = "[ (123, 123),(-,-),(123,inf)]";
		IntervalList I = IntervalUtils.textToIntervalList(text);
		System.out.println(I.toString());

		text = "[ [ (123, 123),(-,-),(123,inf)], [(123, 123),(-,-)],[(123,123),(-,-)]]";
		List<IntervalList> L = IntervalUtils.textToListOfIntervalList(text);
		System.out.println(L.toString());

		text = "[ _I1, _I2, [(1,4),(6,-),(-,-)], _I3]";
		System.out.println(Arrays.asList(text.trim().substring(1, text.length() - 1).split(",")));

		Pattern pattern = Pattern.compile("(\\b_\\w+\\b)");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			System.out.println(matcher.group(0));
		}

		List<String> LElements = new ArrayList<String>();
		List<String> list = Lists.transform(LElements, new Function<String, String>() {
			@Override
			public String apply(String LElement) {
				return LElement;
			}
		});
		System.out.println(list);

	}

}
