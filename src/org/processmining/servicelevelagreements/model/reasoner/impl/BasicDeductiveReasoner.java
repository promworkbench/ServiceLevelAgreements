package org.processmining.servicelevelagreements.model.reasoner.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.processmining.servicelevelagreements.model.Mapping;
import org.processmining.servicelevelagreements.model.eventdatabase.impl.BasicEventDatabase;
import org.processmining.servicelevelagreements.model.interval.IntervalList;
import org.processmining.servicelevelagreements.model.interval.IntervalUtils;
import org.processmining.servicelevelagreements.model.predicate.PredicateParser;
import org.processmining.servicelevelagreements.model.reasoner.Reasoner;
import org.processmining.servicelevelagreements.model.sde.SDEParser;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.xes.classification.XEventCaseClassifier;
import org.processmining.servicelevelagreements.model.xes.classification.XEventInstanceClassifier;
import org.processmining.servicelevelagreements.model.xes.extensions.XCaseExtension;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;

/**
 * Basic reasoner class. Proof of concept, not optimized for efficiency.
 * 
 * TODO [medium] move storage to EventDatabase. Reasoner has logic,
 * EventDatabase has memory.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class BasicDeductiveReasoner extends Reasoner {

	// FIELDS

	// Keeps track of the rules that relate to each fluent
	Map<String, List<String>> fluentToRules;
	// Keeps track of which (statically determined) fluents are affected by which other fluents
	Map<String, List<String>> fluentToFluents;

	//@formatter:off
	Map<String, // Activity lifecycle transition
		Map<String, // Activity concept name
			Map<String, // Activity instance id
				Map<String, // Case concept name
					Map<String, // Resource concept name
						List<String>>>>>> // Set of affected fluents 
							index;
	//@formatter:on

	MutableBiMap<String, String> activityConceptNameMap;
	MutableBiMap<String, String> activityInstanceMap;
	MutableBiMap<String, String> caseConceptNameMap;
	MutableBiMap<String, String> resourceMap;
	MutableBiMap<String, String> slaNameMap;

	XEventLifeTransClassifier xEventLifeTransClassifier;
	XEventNameClassifier xEventNameClassifier;
	XEventInstanceClassifier xEventInstanceClassifier;
	XEventCaseClassifier xEventCaseClassifier;
	XEventResourceClassifier xEventResourceClassifier;
	XTimeExtension xTimeExtension;

	// Memory that keeps bindings for: fluent(arguments), rule, bindings
	//	Table<String, String, Map<String, String>> memory;
	ListMultimap<String, Mapping> longTermMemory;

	// Used for JavaScript engine that evaluates arithmetic
	ScriptEngineManager scriptEngineManager;
	ScriptEngine engine;

	// CONSTRUCTORS

	public BasicDeductiveReasoner() {
		super(new BasicEventDatabase());

		fluentToRules = new HashMap<String, List<String>>();
		fluentToFluents = new HashMap<String, List<String>>();

		//@formatter:off
		index = new HashMap<String, // Activity lifecycle transition
						Map<String, // Activity concept name
							Map<String, // Activity instance id
								Map<String, // Case concept name
									Map<String, // Resource concept name
										List<String>>>>>>(); // Set of affected fluents 
		//@formatter:on

		xEventLifeTransClassifier = new XEventLifeTransClassifier();
		xEventNameClassifier = new XEventNameClassifier();
		xEventInstanceClassifier = new XEventInstanceClassifier();
		xEventCaseClassifier = new XEventCaseClassifier();
		xEventResourceClassifier = new XEventResourceClassifier();
		xTimeExtension = XTimeExtension.instance();

		activityInstanceMap = HashBiMap.newMap();
		activityConceptNameMap = HashBiMap.newMap();
		caseConceptNameMap = HashBiMap.newMap();
		resourceMap = HashBiMap.newMap();
		slaNameMap = HashBiMap.newMap();

		longTermMemory = MultimapBuilder.hashKeys().arrayListValues().build();

		scriptEngineManager = new ScriptEngineManager();
		engine = scriptEngineManager.getEngineByName("JavaScript");

	}

	// METHODS

	/**
	 * Assigns cases to events using the XCaseExtension. Does not modify the
	 * event log when the extension is already present.
	 * 
	 * @param xlog
	 *            The event log.
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void assignCaseAttributes(XLog xlog) {
		if (xlog.getExtensions().contains(XCaseExtension.class))
			return;

		for (int t = 0; t < xlog.size(); t++) {
			XTrace trace = xlog.get(t);
			String caseConceptName = XConceptExtension.instance().extractName(trace);
			if (Strings.isNullOrEmpty(caseConceptName))
				caseConceptName = "case-" + t;
			for (XEvent event : trace) {
				XCaseExtension.instance().assignCase(event, caseConceptName);
			}
		}
	}

	/**
	 * Creates mappings of original Strings to Strings suitable for the RTEC
	 * implementation.
	 * 
	 */
	public void createMappings(XLog xlog, ServiceLevelAgreementsParameters parameters) {

		assignCaseAttributes(xlog);

		XLogInfo logInfo = XLogInfoFactory.createLogInfo(xlog);

		logInfo = XLogInfoFactory.createLogInfo(xlog, xEventNameClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityConceptNameMap.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(activityConceptNameMap.keySet().size() + " activity concept names found in the log.");

		logInfo = XLogInfoFactory.createLogInfo(xlog, xEventInstanceClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityInstanceMap.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(activityInstanceMap.keySet().size() + " activity instances found in the log.");

		logInfo = XLogInfoFactory.createLogInfo(xlog, xEventCaseClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			caseConceptNameMap.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(caseConceptNameMap.keySet().size() + " case concept names found in the log.");

		logInfo = XLogInfoFactory.createLogInfo(xlog, xEventResourceClassifier);
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			resourceMap.put(eventClass.toString(), makeSuitable(eventClass.toString()));
		}
		System.out.println(resourceMap.keySet().size() + " resources found in the log.");

		for (ServiceLevelAgreement sla : parameters.getServiceLevelAgreements()) {
			slaNameMap.put(sla.getName(), makeSuitable(sla.getName()));
		}
		System.out.println(slaNameMap.keySet().size() + " SLAs found in the log.");
	}

	/**
	 * Transforms a String to a String that is suitable for the RTEC
	 * implementation.
	 * 
	 * @param unsuitableString
	 *            The input string.
	 * @return The transformed String.
	 */
	private String makeSuitable(String unsuitableString) {
		String suitableString = unsuitableString;
		suitableString = suitableString.replace("(", "-").replace(")", "-").replace(" ", "-").replaceAll("-", "-")
				.replaceAll(":", "-").replaceAll("<", "-").replaceAll(">", "-").replaceAll("\\+", "-")
				.replaceAll("\\*", "-").replaceAll("/", "-").toLowerCase();
		//.replace(".", "_")
		return suitableString;
	}

	/**
	 * Handles the agreement by building / expanding the index.
	 */
	public void handleAgreement(ServiceLevelAgreement sla) {

		// Walk through the rules of the agreement and store in index.
		for (String templateRule : sla.getTemplate().getRules()) {
			String rule = templateRule;
			rule = rule.replaceAll("%name%", sla.getName());

			for (ServiceLevelAgreementTemplateParameter param : sla.getTemplate().getParameters()) {
				String value = sla.getParameterValues().get(param.getKey()).toString();
				value = makeSuitable(value);
				rule = rule.replaceAll("%" + param.getKey() + "%", value);
			}

			rule = extractHelperFunctions(rule);

			// Store rule for this fluent
			if (!fluentToRules.containsKey(sla.getName()))
				fluentToRules.put(sla.getName(), new ArrayList<String>());
			fluentToRules.get(sla.getName()).add(rule);

			// Split the rule into lines
			String lines[] = rule.split("\\r?\\n");

			for (int line = 0; line < lines.length; line++) {
				String ruleLine = lines[line].trim();

				ruleLine = ruleLine.trim();

				if (ruleLine.endsWith(",") || ruleLine.endsWith(":"))
					ruleLine = ruleLine.substring(0, ruleLine.length() - 1).trim();

				// Only RTEC 'happensAt' predicates have to be considered when building the index.
				// Other predicates and arithmetic will be evaluated when handling an event that matches the index. 
				String[] happensAt = PredicateParser.parseHappensAt(ruleLine);
				// If the rule contains a holdsAt(F(A)=V,T) predicate, then this is information that needs to be stored in the database.
				String[] holdsAt = PredicateParser.parseHoldsAt(ruleLine);
				// If the rule contains a holdsFor(F(A)=V,I) predicate, then this SLA fluent is affected by fluent F.
				String[] holdsFor = PredicateParser.parseHoldsFor(ruleLine);

				if (happensAt != null) {
					// Find the events that occur in this line of the definition.
					String[] event = SDEParser.parseSDE(happensAt[0]);

					// Lookup the 'suitable' name of the event properties in the respective mappings. If the event uses a binding variable (starts with underscore), then replace with a free variable (underscore).
					String lifecycleTransition = event[0];
					String activityConceptName = event[1].charAt(0) == '_' ? "_" : event[1];
					String activityInstanceID = event[2].charAt(0) == '_' ? "_" : event[2];
					String caseConceptName = event[3].charAt(0) == '_' ? "_" : event[3];
					String resource = event[4].charAt(0) == '_' ? "_" : event[4];

					// Store the SLA under this event in the index
					if (!index.containsKey(lifecycleTransition))
						index.put(lifecycleTransition,
								new HashMap<String, Map<String, Map<String, Map<String, List<String>>>>>());
					if (!index.get(lifecycleTransition).containsKey(activityConceptName))
						index.get(lifecycleTransition).put(activityConceptName,
								new HashMap<String, Map<String, Map<String, List<String>>>>());
					if (!index.get(lifecycleTransition).get(activityConceptName).containsKey(activityInstanceID))
						index.get(lifecycleTransition).get(activityConceptName).put(activityInstanceID,
								new HashMap<String, Map<String, List<String>>>());
					if (!index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
							.containsKey(caseConceptName))
						index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
								.put(caseConceptName, new HashMap<String, List<String>>());
					if (!index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
							.get(caseConceptName).containsKey(resource))
						index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
								.get(caseConceptName).put(resource, new ArrayList<String>());
					if (!index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
							.get(caseConceptName).get(resource).contains(sla.getName()))
						index.get(lifecycleTransition).get(activityConceptName).get(activityInstanceID)
								.get(caseConceptName).get(resource).add(sla.getName());
				} else if (holdsAt != null && lines.length == 1) {
					// In case a holdsAt predicate exists by itself, it can only be used to initialize the value of a fluent at a certain time.
					String F = holdsAt[0];
					String A = holdsAt[1];
					String V = holdsAt[2];
					String T = holdsAt[3];
					long ts = Long.parseLong(T);
					getDatabase().initiate(F, A, V, ts);
				} else if (holdsFor != null) {
					// Find out which fluent is fluent F, and store a mapping from F->SLA fluent (the SLA name)
					String F = holdsFor[0];
					// At the moment cyclic dependencies are not supported.
					if (!F.equals(sla.getName())) {
						if (!fluentToFluents.containsKey(F))
							fluentToFluents.put(F, new ArrayList<String>());
						fluentToFluents.get(F).add(sla.getName());
					}
				}
			}

		}

	}

	/**
	 * Extracts helper functions and replaces them by their full-fledged RTEC
	 * specifications.
	 * 
	 * @param instanceRule
	 * @return
	 */
	private String extractHelperFunctions(String instanceRule) {
		//TODO [medium] extract helper functions
		return instanceRule;
	}

	/**
	 * Handles the event by looking up it's affected SLAs in the index.
	 */
	public void handleEvent(XEvent xevent) {
		// Find all properties from the event that needs to be handled		
		String lifecycleTransition = xEventLifeTransClassifier.getClassIdentity(xevent).toLowerCase();
		String activityConceptName = xEventNameClassifier.getClassIdentity(xevent).startsWith("artificial")
				? xEventNameClassifier.getClassIdentity(xevent)
				: activityConceptNameMap.get(xEventNameClassifier.getClassIdentity(xevent));
		String activityInstanceID = xEventInstanceClassifier.getClassIdentity(xevent).startsWith("artificial")
				? xEventInstanceClassifier.getClassIdentity(xevent)
				: activityInstanceMap.get(xEventInstanceClassifier.getClassIdentity(xevent));
		String caseConceptName = caseConceptNameMap.get(xEventCaseClassifier.getClassIdentity(xevent));
		String resource = xEventResourceClassifier.getClassIdentity(xevent).equals("sla-system")
				? xEventResourceClassifier.getClassIdentity(xevent)
				: resourceMap.get(xEventResourceClassifier.getClassIdentity(xevent));
		long timestamp = xTimeExtension.extractTimestamp(xevent).getTime();

		// Find if any fluents need to be re-evaluated.
		Map<String, Map<String, Map<String, Map<String, List<String>>>>> index2 = new HashMap<String, Map<String, Map<String, Map<String, List<String>>>>>();
		Map<String, Map<String, Map<String, List<String>>>> index3 = new HashMap<String, Map<String, Map<String, List<String>>>>();
		Map<String, Map<String, List<String>>> index4 = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<String>> index5 = new HashMap<String, List<String>>();

		List<String> fluentsToReevaluate = new ArrayList<String>();

		if (index.containsKey(lifecycleTransition))
			index2.putAll(index.get(lifecycleTransition));
		if (index.containsKey("_"))
			index2.putAll(index.get("_"));

		if (index2.containsKey(activityConceptName))
			index3.putAll(index2.get(activityConceptName));
		if (index2.containsKey("_"))
			index3.putAll(index2.get("_"));

		if (index3.containsKey(activityInstanceID))
			index4 = index3.get(activityInstanceID);
		if (index3.containsKey("_"))
			index4.putAll(index3.get("_"));

		if (index4.containsKey(caseConceptName))
			index5 = index4.get(caseConceptName);
		if (index4.containsKey("_"))
			index5.putAll(index4.get("_"));

		if (index5.containsKey(resource))
			fluentsToReevaluate.addAll(index5.get(resource));
		if (index5.containsKey("_"))
			fluentsToReevaluate.addAll(index5.get("_"));

		if (!fluentsToReevaluate.isEmpty()) {
			setHandledSDEs(getHandledSDEs() + 1);
			System.out.println("\nHandling event '" + lifecycleTransition + " " + activityConceptName + " "
					+ activityInstanceID + " " + caseConceptName + " " + resource + ", " + timestamp + "'.");

			// Reevaluate the fluents
			for (String fluent : fluentsToReevaluate) {
				updateSimpleFluent(fluent, lifecycleTransition, activityConceptName, activityInstanceID,
						caseConceptName, resource, timestamp + "");
			}
		}
	}

	/**
	 * Re-evaluates the specified simple fluent based on what is currently known
	 * in the database and the event that initialized the re-evaluation.
	 * 
	 * @param fluent
	 * @param lifecycleTransition
	 * @param activityConceptName
	 * @param activityInstanceID
	 * @param caseConceptName
	 * @param resource
	 * @param timestamp
	 */
	private void updateSimpleFluent(String fluent, String lifecycleTransition, String activityConceptName,
			String activityInstanceID, String caseConceptName, String resource, String timestamp) {

		System.out.println("- Updating simple fluent: '" + fluent + "'.");

		// Walk through the rules of the fluent and re-evaluate them
		rule: for (String rule : fluentToRules.get(fluent)) {

			// Split the rule into lines
			String lines[] = rule.split("\\r?\\n");

			/*
			 * Sanity check: we should have at least two lines for a proper rule
			 * that has any effects. Single rules are handled in the
			 * handleAgreement method.
			 */
			if (lines.length < 2) {
				continue rule;
			}

			// Short-term memory to keep track of bindings
			final Mapping localMemory = new Mapping();
			Set<String> unboundedVariables = new HashSet<String>();

			// Find all unbounded binding variables
			// This is done first such that no ordering of predicates is enforced.
			for (int line = 1; line < lines.length; line++) {
				unboundedVariables.addAll(extractBindingVariablesFromText(lines[line]));
			}

			// Walk over the lines and try to find variables to bind
			boolean newBinding = false;
			line: for (int line = 1; line < lines.length; line++) {
				String ruleLine = lines[line].trim();

				String[] happensAtLine = PredicateParser.parseHappensAt(ruleLine);
				String[] holdsAtLine = PredicateParser.parseHoldsAt(ruleLine);
				String[] holdsForLine = PredicateParser.parseHoldsFor(ruleLine);
				String[] union_allLine = PredicateParser.parseUnionAll(ruleLine);
				String[] intersect_allLine = PredicateParser.parseIntersectAll(ruleLine);
				String[] relative_complement_allLine = PredicateParser.parseRelativeComplementAll(ruleLine);

				if (happensAtLine != null) {
					// Will bind the event in the method arguments
					// FYI: If the current fluent is a statically determined fluent affected by a fluent that was affected by the argument event, then this will not match.
					String[] argEvent = SDEParser.parseSDE(happensAtLine[0]);
					String argLifecycleTransition = argEvent[0];
					String argActivityConceptName = argEvent[1];
					String argActivityInstanceID = argEvent[2];
					String argCaseConceptName = argEvent[3];
					String argResource = argEvent[4];
					String argTimestamp = happensAtLine[1];

					//@formatter:off
					if (	
							( isFreeVariable(argLifecycleTransition) || isBindingVariable(argLifecycleTransition) || argLifecycleTransition.equals(lifecycleTransition) )
							&& 
							( isFreeVariable(argActivityConceptName) || isBindingVariable(argActivityConceptName) || argActivityConceptName.equals(activityConceptName) )
							&& 
							( isFreeVariable(argActivityInstanceID) || isBindingVariable(argActivityInstanceID) || argActivityInstanceID.equals(activityInstanceID) )
							&& 
							( isFreeVariable(argCaseConceptName) || isBindingVariable(argCaseConceptName) || argCaseConceptName.equals(caseConceptName) )
							&& 
							( isFreeVariable(argResource) || isBindingVariable(argResource) || argResource.equals(resource) )
							&& 
							( isFreeVariable(argTimestamp) || isBindingVariable(argTimestamp) || argTimestamp.equals(timestamp) )
						)
					{
					//@formatter:on
						if (isBindingVariable(argLifecycleTransition)) {
							localMemory.put(argLifecycleTransition, lifecycleTransition);
							unboundedVariables.remove(argLifecycleTransition);
							newBinding = true;
						}
						if (isBindingVariable(argActivityConceptName)) {
							localMemory.put(argActivityConceptName, activityConceptName);
							unboundedVariables.remove(argActivityConceptName);
							newBinding = true;
						}
						if (isBindingVariable(argActivityInstanceID)) {
							localMemory.put(argActivityInstanceID, activityInstanceID);
							unboundedVariables.remove(argActivityInstanceID);
							newBinding = true;
						}
						if (isBindingVariable(argCaseConceptName)) {
							localMemory.put(argCaseConceptName, caseConceptName);
							unboundedVariables.remove(argCaseConceptName);
							newBinding = true;
						}
						if (isBindingVariable(argResource)) {
							localMemory.put(argResource, resource);
							unboundedVariables.remove(argResource);
							newBinding = true;
						}
						if (isBindingVariable(argTimestamp)) {
							localMemory.put(argTimestamp, timestamp);
							unboundedVariables.remove(argTimestamp);
							newBinding = true;
						}

					}

				} else if (holdsAtLine != null) {
					// The line is a holdsAt predicate
					String F = holdsAtLine[0];
					String A = holdsAtLine[1];
					String V = holdsAtLine[2];
					String T = holdsAtLine[3];

					// Find out any variables to bind and then try to bind them using the database		
					if (unboundedVariables.contains(V) && localMemory.containsKey(T)) {
						long ts = Long.parseLong(localMemory.get(T));
						String value = getDatabase().holdsAt(F, A, ts);
						if (value != null) {
							localMemory.put(V, value);
							unboundedVariables.remove(V);
							newBinding = true;
						}
					}

				} else if (happensAtLine == null && holdsForLine == null && union_allLine == null
						&& intersect_allLine == null && relative_complement_allLine == null) {
					// The line is an expression.

					// If the line (expression) ends in "," it is not the last line.
					if (ruleLine.endsWith(","))
						ruleLine = ruleLine.substring(0, ruleLine.length() - 1);

					// The line holds arithmetic

					// Expand bindings in local memory with those from long-term memory
					mapping: for (Mapping mapping : longTermMemory.get(rule)) {
						// Check if the binding we already have agrees with the one from memory.
						// This means that for all shared variables, they should have the same bindings.
						// We take the first binding that has been stored (so matching is made on a FCFS basis)
						for (String key : mapping.keySet())
							if (localMemory.containsKey(key))
								if (!localMemory.get(key).equals(mapping.get(key)))
									continue mapping;

						// We found an agreeing mapping (there should only be one), so copy the binding to local memory.
						localMemory.putAll(mapping);
						break mapping;
					}
					unboundedVariables.removeAll(localMemory.keySet());

					// Bind all the arguments
					String boundedExpression = ruleLine;
					for (Entry<String, String> entry : localMemory.entrySet()) {
						boundedExpression = boundedExpression.replaceAll("\\b" + entry.getKey() + "\\b",
								entry.getValue());
					}

					try {
						// We assume any row has at most one unbounded variable to bind.
						//TODO [medium] add more logic to bind using expressions using engine.get()
						if (unboundedVariables.size() == 1 && boundedExpression.contains(" = ")) {
							// In case a check is present, change the single equality sign to a double equality sign.
							//							boundedExpression = boundedExpression.replace(" = ", " == ");
							//							System.out.println("- Evaluating '" + boundedExpression + "'");
							Object value = engine.eval(boundedExpression);
							String var = unboundedVariables.iterator().next();
							localMemory.put(var, value.toString());
							unboundedVariables.remove(var);
							newBinding = true;
						}
					} catch (ScriptException e) {
						System.out.println("ERROR - could not evaluate expression '" + boundedExpression + "'");
						e.printStackTrace();
						break line;
					}
				} else {
					// Otherwise we ignore the line since it is not allowed in a definition for a simple fluent.
					System.out.println("WARNING - line not allowed in simple fluent specification (line ignored): '"
							+ ruleLine + "'");
				}

			} // for line

			// If we have no new bindings then we can continue with the next rule.
			if (!newBinding)
				continue rule;

			// Expand bindings in local memory with those from long-term memory
			mapping: for (Mapping mapping : longTermMemory.get(rule)) {
				// Check if the binding we already have agrees with the one from memory.
				// This means that for all shared variables, they should have the same bindings.
				// We take the first binding that has been stored (so matching is made on a FCFS basis)
				for (String key : mapping.keySet())
					if (localMemory.containsKey(key))
						if (!localMemory.get(key).equals(mapping.get(key)))
							continue mapping;

				// We found an agreeing mapping (there should only be one), so copy the binding to local memory.
				localMemory.putAll(mapping);
				break mapping;
			}
			unboundedVariables.removeAll(localMemory.keySet());

			// Check if we can evaluate the rule by checking if every binding variable is bounded.
			if (unboundedVariables.isEmpty()) {
				// All variables that need to be bounded are bounded, we can evaluate the rule.

				// Check whether all the lines of this rule evaluate to true, if not, continue with the next rule!
				boolean allLinesHold = true;
				line: for (int line = 1; line < lines.length; line++) {
					String ruleLine = lines[line].trim();

					// Check whether the line is negated
					boolean negate = false;
					if (ruleLine.startsWith("!")) {
						negate = true;
						ruleLine = ruleLine.substring(1, ruleLine.length());
					}

					String[] holdsAt = PredicateParser.parseHoldsAt(ruleLine);

					// Evaluate the line
					if (holdsAt != null) {
						// The line is a holdsAt predicate
						String F = holdsAt[0];
						String A = holdsAt[1];
						String V = holdsAt[2];
						String T = holdsAt[3];

						// Bind all the arguments and the timestamp
						List<String> AElements = extractBindingVariablesFromText(A);
						// If only we could use Java 8...
						List<String> args = Lists.transform(AElements, new Function<String, String>() {
							@Override
							public String apply(String AElement) {
								return localMemory.get(AElement);
							}
						});
						if (V.startsWith("_")) {
							V = localMemory.get(V);
						}
						long initTimestamp = Long.parseLong(localMemory.get(T));

						if (negate == getDatabase().holdsAt(F, toStringWithoutBrackets(args), V, initTimestamp)) {
							allLinesHold = false;
							break line;
						}

					} else {

						/*
						 * The line either holds arithmetic or one of the
						 * following predicates: happensAt, holdsFor, union_all,
						 * intersect_all, relative_complement_all
						 */
						String[] happensAtLine = PredicateParser.parseHappensAt(ruleLine);
						String[] holdsForLine = PredicateParser.parseHoldsFor(ruleLine);
						String[] union_allLine = PredicateParser.parseUnionAll(ruleLine);
						String[] intersect_allLine = PredicateParser.parseIntersectAll(ruleLine);
						String[] relative_complement_allLine = PredicateParser.parseRelativeComplementAll(ruleLine);

						if (happensAtLine == null && holdsForLine == null && union_allLine == null
								&& intersect_allLine == null && relative_complement_allLine == null) {

							// If the line (expression) ends in "," it is not the last line.
							if (ruleLine.endsWith(","))
								ruleLine = ruleLine.substring(0, ruleLine.length() - 1);

							// The line holds arithmetic
							// Bind all the arguments
							String boundedExpression = ruleLine;
							for (Entry<String, String> entry : localMemory.entrySet()) {
								boundedExpression = boundedExpression.replaceAll("\\b" + entry.getKey() + "\\b",
										entry.getValue());
							}
							// In case a check is present, change the single equality sign to a double equality sign.
							boundedExpression = boundedExpression.replace(" = ", " == ");
							try {
								//System.out.println("- Evaluating '" + boundedExpression + "'");
								if (negate == (boolean) engine.eval(boundedExpression)) {
									allLinesHold = false;
									break line;
								}
							} catch (ScriptException e) {
								System.out.println("ERROR - could not evaluate expression '" + boundedExpression + "'");
								e.printStackTrace();
							}
						} else {
							// Otherwise we ignore the line since it's either a happensAt predicate or it is not allowed in a definition for a simple fluent.
						}
					}
				} // for line

				// Apply the effects
				if (allLinesHold) {

					System.out.println("- Rule " + rule);

					// Check the first line to get the fluent arguments (they should be binded by now)
					String firstLine = lines[0].trim();
					String[] initiatedAt = PredicateParser.parseInitiatedAt(firstLine);
					String[] terminatedAt = PredicateParser.parseTerminatedAt(firstLine);

					if (initiatedAt != null) {
						String F = initiatedAt[0];
						String A = initiatedAt[1];
						String V = initiatedAt[2];
						String T = initiatedAt[3];

						if (F.startsWith("_")) {
							// Technically this should never be needed
							F = localMemory.get(F);
						}
						if (A.startsWith("_")) {
							A = localMemory.get(A);
						}
						if (V.startsWith("_")) {
							V = localMemory.get(V);
						}
						long TValue = Long.parseLong(localMemory.get(T));

						getDatabase().initiate(F, A, V, TValue);
						System.out.println("- Binding: " + localMemory.toString());
					} else if (terminatedAt != null) {
						String F = terminatedAt[0];
						String A = terminatedAt[1];
						String V = terminatedAt[2];
						String T = terminatedAt[3];

						if (F.startsWith("_")) {
							// Technically this should never be needed
							F = localMemory.get(F);
						}
						if (A.startsWith("_")) {
							A = localMemory.get(A);
						}
						if (V.startsWith("_")) {
							V = localMemory.get(V);
						}
						long TValue = Long.parseLong(localMemory.get(T));

						getDatabase().terminate(F, A, V, TValue);
						System.out.println("- Binding: " + localMemory.toString());
					}

					// Remove bindings from memory
					for (Iterator<Mapping> it = longTermMemory.get(rule).iterator(); it.hasNext();) {
						Mapping mapping = it.next();
						if (localMemory.entrySet().containsAll(mapping.entrySet()))
							it.remove();
					}

					/*
					 * Check on which statically determined fluents are affected
					 * by this re-evaluated simple fluent so we can update those
					 * as well.
					 */
					if (fluentToFluents.containsKey(fluent)) {
						for (String f : fluentToFluents.get(fluent)) {
							updateStaticallyDeterminedFluent(f, fluent, localMemory);
						}
					}
				}
			} else {
				// We cannot evaluate the full rule yet, so store in memory and continue
				longTermMemory.put(rule, localMemory);
			}

		} // for each rule
	}

	/**
	 * Updates statically determined fluents.
	 * 
	 * @param fluent
	 * @param originatingFluent
	 * @param originatingFluentArgumentsBindingVariables
	 */
	private void updateStaticallyDeterminedFluent(String fluent, String originatingFluent,
			Map<String, String> originatingFluentArgumentsBindingVariables) {

		System.out.println(
				"- Updating statically determined fluent: '" + fluent + "' (from '" + originatingFluent + "').");

		// Walk through the rules of the fluent and re-evaluate them
		rule: for (String rule : fluentToRules.get(fluent)) {
			System.out.println("- Rule " + rule);

			// Split the rule into lines
			String lines[] = rule.split("\\r?\\n");

			// Short-term memory to keep track of bindings
			final Mapping localMemory = new Mapping();
			Set<String> unboundedVariables = new HashSet<String>();

			// Check the first line to get the fluent arguments (they should be binded by now)
			String firstLine = lines[0].trim();

			String[] holdsFor = PredicateParser.parseHoldsFor(firstLine);

			String fluentArgumentsText = "";
			if (holdsFor != null) {
				fluentArgumentsText = holdsFor[1];
			}

			// Find all unbounded binding variables
			// This is done first such that no ordering of predicates is enforced.
			for (int line = 1; line < lines.length; line++) {
				unboundedVariables.addAll(extractBindingVariablesFromText(lines[line]));
			}

			// Get all the bindings for the binding variables in the fluent arguments
			Map<String, String> fluentArgumentsBindingVariables = new HashMap<String, String>();

			// Expand local memory with memory from originating fluent
			//TODO [medium] Implement logic to remove assumption that binding variables in statically determined fluent share the label with their originating fluent.
			localMemory.putAll(originatingFluentArgumentsBindingVariables);

			List<String> fluentArgumentsList = extractBindingVariablesFromText(fluentArgumentsText);
			for (String bindingVariable : fluentArgumentsList) {
				if (localMemory.containsKey(bindingVariable))
					fluentArgumentsBindingVariables.put(bindingVariable, localMemory.get(bindingVariable));
				else
					System.out.println("ERROR - Unknown fluent arguments binding variable.");
			}

			String fluentArgumentsTextBound = fluentArgumentsText;
			for (Entry<String, String> entry : fluentArgumentsBindingVariables.entrySet()) {
				fluentArgumentsTextBound = fluentArgumentsTextBound.replaceAll("\\b" + entry.getKey() + "\\b",
						entry.getValue());
			}

			// Expand bindings in local memory with those from long-term memory
			mapping: for (Mapping mapping : longTermMemory.get(rule)) {
				// Check if the binding we already have agrees with the one from memory.
				// This means that for all shared variables, they should have the same bindings.
				// We take the first binding that has been stored (so matching is made on a FCFS basis)
				for (String key : mapping.keySet())
					if (localMemory.containsKey(key))
						if (!localMemory.get(key).equals(mapping.get(key)))
							continue mapping;

				// We found an agreeing mapping (there should only be one), so copy the binding to local memory.
				localMemory.putAll(mapping);
				break mapping;
			}
			unboundedVariables.removeAll(localMemory.keySet());

			// Walk over the lines and try to find variables to bind
			boolean newBinding = false;
			line: for (int line = 1; line < lines.length; line++) {
				String ruleLine = lines[line].trim();

				String[] holdsForLine = PredicateParser.parseHoldsFor(ruleLine);
				String[] union_allLine = PredicateParser.parseUnionAll(ruleLine);
				String[] intersect_allLine = PredicateParser.parseIntersectAll(ruleLine);
				String[] relative_complement_allLine = PredicateParser.parseRelativeComplementAll(ruleLine);

				if (holdsForLine != null) {
					// Will bind the output variable
					String F = holdsForLine[0];
					String V = holdsForLine[2];
					String I = holdsForLine[3];

					if (isBindingVariable(I)) {
						IntervalList Ivalue = getDatabase().holdsFor(F, fluentArgumentsTextBound, V);
						localMemory.put(I, Ivalue.toString());
						unboundedVariables.remove(I);
						newBinding = true;
					}
				}
				if (union_allLine != null) {
					// Will bind the output variable
					String L = union_allLine[0];
					String I = union_allLine[1];

					List<String> LElements = extractBindingVariablesFromText(L);

					boolean allLElementsBounded = localMemory.keySet().containsAll(LElements);

					if (isBindingVariable(I) && allLElementsBounded) {
						// If only we could use Java 8...
						List<IntervalList> list = Lists.transform(LElements, new Function<String, IntervalList>() {
							@Override
							public IntervalList apply(String LElement) {
								return IntervalUtils.textToIntervalList(localMemory.get(LElement));
							}
						});
						IntervalList Ivalue = IntervalUtils.union_all(list);
						localMemory.put(I, Ivalue.toString());
						unboundedVariables.remove(I);
						newBinding = true;
					}

				}
				if (intersect_allLine != null) {
					// Will bind the output variable
					String L = intersect_allLine[0];
					String I = intersect_allLine[1];

					List<String> LElements = extractBindingVariablesFromText(L);

					boolean allLElementsBounded = localMemory.keySet().containsAll(LElements);

					if (isBindingVariable(I) && allLElementsBounded) {
						// If only we could use Java 8...
						List<IntervalList> list = Lists.transform(LElements, new Function<String, IntervalList>() {
							@Override
							public IntervalList apply(String LElement) {
								return IntervalUtils.textToIntervalList(localMemory.get(LElement));
							}
						});

						IntervalList Ivalue = IntervalUtils.intersect_all(list);
						localMemory.put(I, Ivalue.toString());
						unboundedVariables.remove(I);
						newBinding = true;
					}

				}
				if (relative_complement_allLine != null) {
					// Will bind the output variable
					String Iprime = relative_complement_allLine[0];
					String L = relative_complement_allLine[1];
					String I = relative_complement_allLine[2];

					List<String> LElements = extractBindingVariablesFromText(L);

					boolean allLElementsBounded = localMemory.keySet().containsAll(LElements);

					if (isBindingVariable(I) && allLElementsBounded) {
						// If only we could use Java 8...
						List<IntervalList> list = Lists.transform(LElements, new Function<String, IntervalList>() {
							@Override
							public IntervalList apply(String LElement) {
								return IntervalUtils.textToIntervalList(localMemory.get(LElement));
							}
						});
						IntervalList Ivalue = IntervalUtils.relative_complement_all(
								IntervalUtils.textToIntervalList(localMemory.get(Iprime)), list);
						localMemory.put(I, Ivalue.toString());
						unboundedVariables.remove(I);
						newBinding = true;
					}

				}

			} // for line

			if (!newBinding)
				continue rule;

			unboundedVariables.removeAll(localMemory.keySet());

			// Check if we can evaluate the rule by checking if every binding variable is bounded.
			if (unboundedVariables.isEmpty()) {
				// All variables that need to be bounded are bounded, we can evaluate the rule.
				if (holdsFor != null) {

					String V = holdsFor[2];
					String I = holdsFor[3];

					IntervalList Ivalue = IntervalUtils.textToIntervalList(localMemory.get(I));

					if (!Ivalue.isEmpty()) {

						getDatabase().set(fluent, fluentArgumentsTextBound, V, Ivalue);
						System.out.println("- Binding: " + localMemory.toString());
					}
				}

				// Remove bindings from memory
				for (Iterator<Mapping> it = longTermMemory.get(rule).iterator(); it.hasNext();) {
					Mapping mapping = it.next();
					if (!localMemory.entrySet().containsAll(mapping.entrySet()))
						it.remove();
				}
			} else {
				// We cannot evaluate the full rule yet, so store in memory and continue
				longTermMemory.put(rule, localMemory);
			}

			// Check on which other fluents the re-evaluated fluents have an effect so we can update those as well.
			if (fluentToFluents.containsKey(fluent)) {
				for (String f : fluentToFluents.get(fluent)) {
					updateStaticallyDeterminedFluent(f, fluent, fluentArgumentsBindingVariables);
				}

			}

		} // for rule

	}

	/**
	 * Checks whether the provided variable is a free variable (equals '_').
	 * 
	 * @param variable
	 * @return
	 */
	private boolean isFreeVariable(String variable) {
		return variable.equals("_");
	}

	/**
	 * Checks whether the provided variable is a binding variable (starts with
	 * '_').
	 * 
	 * @param variable
	 * @return
	 */
	private boolean isBindingVariable(String variable) {
		return variable.startsWith("_") && variable.length() > 1;
	}

	/**
	 * Extracts all binding variable elements from the provided text. For
	 * example for the list: "[ _I1, _I2, [(1,4),(6,-),(-,-)], _I3]", the method
	 * will return [_I1,_I2,_I3]. If the list contains no binding variables an
	 * empty list is returned.
	 * 
	 * @param text
	 * @return
	 */
	private List<String> extractBindingVariablesFromText(String text) {
		Pattern pattern = Pattern.compile("(\\b_\\w+\\b)");
		Matcher matcher = pattern.matcher(text);
		// Using .matches() already removes the first match! Keep commented out for future reference.
		//		if (!matcher.matches())
		//			return new ArrayList<String>();
		List<String> elements = new ArrayList<String>(matcher.groupCount());
		while (matcher.find()) {
			elements.add(matcher.group(0));
		}
		return elements;
	}

	/**
	 * Prints the list "[a, b, c]" as "a, b, c".
	 * 
	 * @param list
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private String toStringWithoutBrackets(List list) {
		if (list.size() == 0)
			return "";
		return list.toString().substring(1, list.toString().length() - 1).trim();
	}

}
