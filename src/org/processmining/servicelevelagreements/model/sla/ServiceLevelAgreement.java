package org.processmining.servicelevelagreements.model.sla;

import java.util.HashMap;
import java.util.Map;

/**
 * Constraints have a name and keep a reference to the constraint template they
 * were created from. They also hold a map with values for the parameters in the
 * template.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class ServiceLevelAgreement {

	// FIELDS

	private String name;
	private ServiceLevelAgreementTemplate template;
	private Map<String, Object> parameterValues;

	// CONSTRUCTORS

	public ServiceLevelAgreement() {
		parameterValues = new HashMap<String, Object>();
	}

	// GETTERS AND SETTERS

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceLevelAgreementTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ServiceLevelAgreementTemplate template) {
		this.template = template;
	}

	public Map<String, Object> getParameterValues() {
		return parameterValues;
	}

	public void setParameterValues(Map<String, Object> parameterValues) {
		this.parameterValues = parameterValues;
	}

	public Object setParameterValue(String key, Object value) {
		return this.parameterValues.put(key, value);
	}

	// METHODS

	@Override
	public String toString() {
		return template.getName() + " \"" + name + "\" " + parameterValues.toString();
	}

}
