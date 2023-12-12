package org.processmining.servicelevelagreements.model;

import java.util.List;
import java.util.Map;

import org.processmining.framework.util.Pair;

import gnu.trove.map.hash.THashMap;

public class ConstraintLog {

	private Map<String, Map<String, Map<String, List<Pair<String, String>>>>> output;

	public ConstraintLog() {
		setOutput(new THashMap<String, Map<String, Map<String, List<Pair<String, String>>>>>());
	}

	public Map<String, Map<String, Map<String, List<Pair<String, String>>>>> getOutput() {
		return output;
	}

	public void setOutput(Map<String, Map<String, Map<String, List<Pair<String, String>>>>> output) {
		this.output = output;
	}

	public void addOutput(String constraint, String status, String argument, List<Pair<String, String>> intervals) {
		if (!output.containsKey(constraint))
			output.put(constraint, new THashMap<String, Map<String, List<Pair<String, String>>>>());
		if (!output.get(constraint).containsKey(status))
			output.get(constraint).put(status, new THashMap<String, List<Pair<String, String>>>());
		if (!output.get(constraint).get(status).containsKey(argument))
			output.get(constraint).get(status).put(argument, intervals);
	}
}
