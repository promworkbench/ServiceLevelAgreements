package org.processmining.servicelevelagreements.test;

import RTEC.Execute.WindowHandler;

public class ScaRTECTest {

	public static void main(String[] args) {
		testFromScalaCode();
		//		test();
	}

	private static void testFromScalaCode() {
		//		String inputDir = "E:/Eclipse workspace/ServiceLevelAgreements/src/org/processmining/servicelevelagreements/test/example/pm_toy";
		//		String outputFile = "E:/Eclipse workspace/ServiceLevelAgreements/src/org/processmining/servicelevelagreements/test/example/pm_toy/recognition.txt";
		//		int slidingStep = 139-83;
		//		int windowSize = 139-83;
		//		int startTime = 139;
		//		int lastTime = 139;
		//		int clock = 1;

		String inputDir = "E:/Eclipse workspace/ServiceLevelAgreements/src/org/processmining/servicelevelagreements/test/example/bart";
		String outputFile = "E:/Eclipse workspace/ServiceLevelAgreements/src/org/processmining/servicelevelagreements/test/example/bart/recognition.txt";
		//		int slidingStep = 1887180000 - 20160500;
		//		int windowSize = 1887180000 - 20160500;
		int startTime = 1887180000;
		int lastTime = 1887180000;
		int clock = 1;

		// set input directory and output file
		WindowHandler.setIOParameters(inputDir, outputFile);
		// start event recognition loop
		WindowHandler.performER(lastTime - startTime, lastTime - startTime, lastTime, lastTime, clock);

	}

	//	private static void test() {
	//		InstantEventId start = new InstantEventId("start", 0);
	//		InstantEventId complete = new InstantEventId("complete", 0);
	//
	//		//		RTEC.Data.InstantEvent e = new InstantEvent(RTEC.Data.InputIE, "testEvent", 1);
	//
	//		//		Predicate p = new HappensAtPredicate(HappensAtIE.apply(arg0, arg1, arg2), body)
	//		//		HappensAtIE.apply(start, new Set.Set1<>("s1").toSeq();, "");
	//
	//		Reasoner reasoner = new Reasoner();		
	//	}

}
