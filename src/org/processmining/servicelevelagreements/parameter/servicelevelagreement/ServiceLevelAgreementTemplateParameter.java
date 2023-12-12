package org.processmining.servicelevelagreements.parameter.servicelevelagreement;

/**
 * Parameters object for SLA templates.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class ServiceLevelAgreementTemplateParameter {

	private String key;
	private ServiceLevelAgreementTemplateParameterType type;
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ServiceLevelAgreementTemplateParameterType getType() {
		return type;
	}

	public void setType(ServiceLevelAgreementTemplateParameterType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
