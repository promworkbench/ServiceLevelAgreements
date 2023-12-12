package org.processmining.servicelevelagreements.algorithm.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.processmining.framework.util.Pair;
import org.processmining.servicelevelagreements.model.ConstraintLog;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.xes.classification.XEventCaseClassifier;
import org.processmining.servicelevelagreements.model.xes.classification.XEventInstanceClassifier;
import org.processmining.servicelevelagreements.model.xes.extensions.XCaseExtension;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameterType;

import com.google.common.base.Strings;

import RTEC.Execute.WindowHandler;

public class CurrentServiceLevelAgreementsAlgorithm {

	XLog eventlog;

	//TODO [medium] Optimization - I think all of these can be normal, one-directional maps
	MutableBiMap<String, String> activityInstanceMapping;
	MutableBiMap<String, String> activityConceptNameMapping;
	MutableBiMap<String, String> caseConceptNameMapping;
	MutableBiMap<String, String> constraintNameMapping;

	XEventNameClassifier xEventNameClassifier;
	XEventInstanceClassifier xEventInstanceClassifier;
	XEventCaseClassifier xEventCaseClassifier;
	XEventLifeTransClassifier xEventLifeTransClassifier;
	XTimeExtension xTimeExtension;

	long slidingStep;
	long windowSize;
	long startTime;
	long lastTime;
	int clock;

	protected ConstraintLog apply(XLog xlog, ServiceLevelAgreementsParameters parameters) {
		if (parameters.isCloneEventLog())
			eventlog = (XLog) xlog.clone();
		else
			eventlog = xlog;

		assignCaseAttributes(eventlog);

		xEventNameClassifier = new XEventNameClassifier();
		xEventInstanceClassifier = new XEventInstanceClassifier();
		xEventCaseClassifier = new XEventCaseClassifier();
		xEventLifeTransClassifier = new XEventLifeTransClassifier();
		xTimeExtension = XTimeExtension.instance();
		activityInstanceMapping = HashBiMap.newMap();
		activityConceptNameMapping = HashBiMap.newMap();
		caseConceptNameMapping = HashBiMap.newMap();
		constraintNameMapping = HashBiMap.newMap();

		slidingStep = 1000 / 2;
		windowSize = 1000;
		startTime = Long.MAX_VALUE;
		lastTime = Long.MIN_VALUE;
		clock = 1000;

		/**
		 * Since ScaRTEC (the Scala-based EC / RTEC implementation we use)
		 * cannot deal with spaces in names of activities, instances, or cases,
		 * we keep a mapping.
		 */
		createMappings(parameters);

		/**
		 * Create a temporary folder that will hold the ScaRTEC dataset file,
		 * definitions, and declarations.
		 */
		Path tempDir = createTemporaryFiles();

		//Translate eventlog to ScaRTEC dataset using the different mappings
		createDataset(tempDir, eventlog);

		// Translate the constraints to ScaRTEC declarations
		createDeclarations(tempDir, parameters.getServiceLevelAgreements());

		// Translate the constraints to ScaRTEC definitions
		createDefinitions(tempDir, parameters.getServiceLevelAgreements());

		// Run ScaRTEC event recognition and output into the temporary directory
		Path recognitionPath = tempDir.resolve("recognition.txt");
		WindowHandler.setIOParameters(tempDir.toString(), recognitionPath.toString());

		// Start event recognition (for now, use one single batch)
		//TODO [low] make use of windowing technique
		startTime += slidingStep;
		System.out.println("windowsize: " + windowSize);
		System.out.println("slidingStep: " + slidingStep);
		System.out.println("startTime: " + startTime);
		System.out.println("lastTime: " + lastTime);
		System.out.println("clock: " + clock);
		System.out.println("Starting ER");
		WindowHandler.performER(lastTime - startTime, lastTime - startTime, lastTime, lastTime, clock);
		System.out.println("Finished ER");

		System.out.println("Create output");
		ConstraintLog constraintLog = parseOutput(recognitionPath);

		// Delete our temporary ScaRTEC files now we have the result.
		System.out.println("Remove temporary files");
		recursivelyDeletePath(tempDir);

		return constraintLog;
	}

	/**
	 * arse ScaRTEC output into a ConstraintLog object At the moment, we're only
	 * interested in the MVIs of the constraints.
	 * 
	 * @param recognitionPath
	 *            The Path to the file that contains the ScaRTEC output.
	 * @return The constraint log.
	 */
	private ConstraintLog parseOutput(Path recognitionPath) {
		ConstraintLog constraintLog = new ConstraintLog();
		try (
				BufferedReader reader = Files.newBufferedReader(recognitionPath, StandardCharsets.UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				// We don't care about the lifecycleState fluents for now, only about the constraints themselves.
				if (line.startsWith("lifecycleState"))
					continue;

				//				System.out.println(line);

				Pattern pattern = Pattern.compile("(\\w+)\\((\\w+)\\)=(\\w+),\\[(\\(\\w+,\\w+\\),?)+\\]");
				Matcher matcher = pattern.matcher(line);

				while (matcher.find()) {

					// Find the constraint, its status and its argument
					String constraint = matcher.group(1);
					String argument = matcher.group(2);
					String status = matcher.group(3);

					// Find all intervals
					String interval = matcher.group(4);
					pattern = Pattern.compile("\\((\\w+),(\\w+)\\)");
					matcher = pattern.matcher(interval);

					List<Pair<String, String>> intervals = new ArrayList<Pair<String, String>>();
					while (matcher.find()) {
						intervals.add(new Pair<String, String>(matcher.group(1), matcher.group(2)));
					}

					constraintLog.addOutput(constraint, status, argument, intervals);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return constraintLog;
	}

	/**
	 * Creates a ScaRTEC definitions.txt from the event log in the specified
	 * Path directory.
	 * 
	 * @param tempDir
	 *            The directory to create the file in.
	 * @param constraints
	 *            The constraints.
	 */
	private void createDefinitions(Path tempDir, List<ServiceLevelAgreement> constraints) {
		Path definitionsPath = tempDir.resolve("definitions.txt");
		try (
				BufferedWriter writer = Files.newBufferedWriter(definitionsPath, StandardCharsets.UTF_8)) {

			// Default lines for activity lifecycles
			writer.write(
					"> InitiatedAt [lifecycleState ActivityInstanceId ActivityConceptName CaseConceptName = started] T");
			writer.newLine();
			writer.write("	HappensAt [start ActivityInstanceId ActivityConceptName CaseConceptName] T");
			writer.newLine();
			writer.newLine();
			writer.write(
					"> InitiatedAt [lifecycleState ActivityInstanceId ActivityConceptName CaseConceptName = completed] T");
			writer.newLine();
			writer.write("	HappensAt [complete ActivityInstanceId ActivityConceptName CaseConceptName] T");
			writer.newLine();
			writer.newLine();

			// Constraints
			for (ServiceLevelAgreement constraint : constraints) {
				for (String rule : materialize(constraint)) {
					writer.write(rule);
					writer.newLine();
				}
				writer.newLine();
			}
			writer.newLine();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Materializes a constraint by taking the rules defined by it's template
	 * and replacing the parameters with their values.
	 * 
	 * @param constraint
	 *            The constraint.
	 * @return A list of materialized rules.
	 */
	private List<String> materialize(ServiceLevelAgreement constraint) {
		List<String> rules = new ArrayList<String>();

		for (String templateRule : constraint.getTemplate().getRules()) {
			String rule = templateRule;
			rule = rule.replaceAll("%CONSTRAINT%", constraintNameMapping.get(constraint.getName()));
			for (ServiceLevelAgreementTemplateParameter param : constraint.getTemplate().getParameters()) {
				String value = constraint.getParameterValues().get(param.getKey()).toString();
				String suitableValue = value;
				if (param.getType().equals(ServiceLevelAgreementTemplateParameterType.ACTIVITY))
					suitableValue = activityConceptNameMapping.get(value);

				rule = rule.replaceAll("%" + param.getKey() + "%", suitableValue);
			}
			rules.add("> " + rule);
		}

		return rules;

	}

	/**
	 * Creates a ScaRTEC declarations.txt from the event log in the specified
	 * Path directory.
	 * 
	 * @param tempDir
	 *            The directory to create the file in.
	 * @param constraints
	 *            The constraints.
	 */
	private void createDeclarations(Path tempDir, List<ServiceLevelAgreement> constraints) {
		Path declarationsPath = tempDir.resolve("declarations.txt");
		try (
				BufferedWriter writer = Files.newBufferedWriter(declarationsPath, StandardCharsets.UTF_8)) {

			writer.write("InstantEvents {");
			writer.newLine();
			writer.write("    Input: [start 3]");
			writer.newLine();
			writer.write("    Input: [complete 3]");
			writer.newLine();
			writer.write("}");
			writer.newLine();
			writer.newLine();

			writer.write("Fluents {");
			writer.newLine();
			writer.write("    Simple: [lifecycleState 3 = started]");
			writer.newLine();
			writer.write("    Simple: [lifecycleState 3 = completed]");
			writer.newLine();
			writer.newLine();
			for (ServiceLevelAgreement constraint : constraints) {
				writer.write(
						String.format("    Simple: [%s 1 = pending]", constraintNameMapping.get(constraint.getName())));
				writer.newLine();
				writer.write(String.format("    Simple: [%s 1 = satisfied]",
						constraintNameMapping.get(constraint.getName())));
				writer.newLine();
				writer.write(String.format("    Simple: [%s 1 = violated]",
						constraintNameMapping.get(constraint.getName())));
				writer.newLine();
			}
			writer.write("}");
			writer.newLine();
			writer.newLine();

			writer.write("InputEntities {");
			writer.newLine();
			writer.write("    InputStart 3:");
			writer.newLine();
			writer.write("    	[start]");
			writer.newLine();
			writer.write("    InputComplete 3:");
			writer.newLine();
			writer.write("    	[complete]");
			writer.newLine();
			writer.write("}");
			writer.newLine();
			writer.newLine();

			writer.write("BuiltEntities {");
			writer.newLine();
			writer.write("	ActivityInstanceActivityCase 3:");
			writer.newLine();
			writer.write("		[InputStart()]");
			writer.newLine();
			writer.write("		[InputComplete()]");
			writer.newLine();
			writer.write("	Instance 1:");
			writer.newLine();
			writer.write("		[InputStart(0,1)]");
			writer.newLine();
			writer.write("		[InputComplete(0,1)]");
			writer.newLine();
			writer.write("	Activity 1:");
			writer.newLine();
			writer.write("		[InputStart(1,2)]");
			writer.newLine();
			writer.write("		[InputComplete(1,2)]");
			writer.newLine();
			writer.write("	Case 1:");
			writer.newLine();
			writer.write("		[InputStart(2,3)]");
			writer.newLine();
			writer.write("		[InputComplete(2,3)]");
			writer.newLine();
			writer.write("}");
			writer.newLine();
			writer.newLine();

			writer.write("CachingOrder {");
			writer.newLine();
			writer.write("    [lifecycleState 3 = _]	-> ActivityInstanceActivityCase");
			writer.newLine();
			for (ServiceLevelAgreement constraint : constraints) {
				writer.write(String.format("    [%s 1 = _] -> %s", constraintNameMapping.get(constraint.getName()),
						constraint.getTemplate().getProcessEntity().getShortDescription()));
				writer.newLine();
			}
			writer.write("}");
			writer.newLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a ScaRTEC dataset.txt from the event log in the specified Path
	 * directory.
	 * 
	 * @param tempDir
	 *            The directory to create the file in.
	 * @param eventlog
	 *            The event log.
	 */
	private void createDataset(Path tempDir, XLog eventlog) {
		if (!tempDir.toFile().isDirectory())
			return;

		Path datasetPath = tempDir.resolve("dataset.txt");

		try (
				BufferedWriter writer = Files.newBufferedWriter(datasetPath, StandardCharsets.UTF_8)) {

			for (XTrace trace : eventlog) {
				for (XEvent event : trace) {

					StandardModel transition = XLifecycleExtension.instance().extractStandardTransition(event);
					if (!(transition.equals(StandardModel.START) || transition.equals(StandardModel.COMPLETE)))
						continue;

					long time = xTimeExtension.extractTimestamp(event).getTime();
					if (time < startTime)
						startTime = time;
					if (time > lastTime)
						lastTime = time;

					//	example: "HappensAt [complete instance1 activityA case1] 1502705767"
					writer.write(String.format("HappensAt [%s %s %s %s] %s", transition.toString().toLowerCase(),
							activityInstanceMapping.get(xEventInstanceClassifier.getClassIdentity(event)),
							activityConceptNameMapping.get(xEventNameClassifier.getClassIdentity(event)),
							caseConceptNameMapping.get(xEventCaseClassifier.getClassIdentity(event)), time));
					writer.newLine();

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates a temporary folder that holds the ScaRTEC dataset, definitions,
	 * and declarations files.
	 * 
	 * @return The path of the temporary folder.
	 */
	private Path createTemporaryFiles() {
		try {
			Path tempDir = Files.createTempDirectory("prom-sla-tmp");
			Files.createFile(tempDir.resolve("dataset.txt"));
			Files.createFile(tempDir.resolve("declarations.txt"));
			Files.createFile(tempDir.resolve("definitions.txt"));
			Files.createFile(tempDir.resolve("recognition.txt"));
			System.out.println("Create temporary files in " + tempDir);
			return tempDir;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Recursively deletes the given Path. If the Path refers to a directory, it
	 * will delete the directory and all files and other directories in it.
	 * 
	 * @param path
	 *            The Path to delete.
	 */
	private void recursivelyDeletePath(Path path) {
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			System.out.println("Deleted folder " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates mappings of original Strings to Strings suitable for ScaRTEC.
	 * 
	 * @param parameters
	 */
	private void createMappings(ServiceLevelAgreementsParameters parameters) {
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(eventlog);

		logInfo = XLogInfoFactory.createLogInfo(eventlog, xEventInstanceClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityInstanceMapping.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(activityInstanceMapping.keySet().size() + " activity instances found.");
		logInfo = XLogInfoFactory.createLogInfo(eventlog, xEventNameClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityConceptNameMapping.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(activityConceptNameMapping.keySet().size() + " activity concept names found.");
		logInfo = XLogInfoFactory.createLogInfo(eventlog, xEventCaseClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			caseConceptNameMapping.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(caseConceptNameMapping.keySet().size() + " case concept names found.");
		for (ServiceLevelAgreement constraint : parameters.getServiceLevelAgreements()) {
			constraintNameMapping.put(constraint.getName(), makeSuitable(constraint.getName()));
		}
	}

	/**
	 * Transforms a String to a String that is suitable for ScaRTEC.
	 * 
	 * @param unsuitableString
	 *            The input string.
	 * @return The transformed String.
	 */
	private String makeSuitable(String unsuitableString) {
		String suitableString = unsuitableString;
		suitableString = suitableString.replace("(", "").replace(")", "").replace(" ", "_").replace(".", "_")
				.replaceAll("-", "_").toLowerCase();
		return suitableString;
	}

	/**
	 * Assigns cases to events using the XCaseExtension. Does not modify the
	 * event log when the extension is already present.
	 * 
	 * @param eventlog
	 *            The event log.
	 */
	private void assignCaseAttributes(XLog eventlog) {
		if (eventlog.getExtensions().contains(XCaseExtension.class))
			return;

		for (int t = 0; t < eventlog.size(); t++) {
			XTrace trace = eventlog.get(t);
			String caseConceptName = XConceptExtension.instance().extractName(trace);
			if (Strings.isNullOrEmpty(caseConceptName))
				caseConceptName = "trace" + t;
			for (XEvent event : trace) {
				XCaseExtension.instance().assignCase(event, caseConceptName);
			}
		}

	}

}
