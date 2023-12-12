package org.processmining.servicelevelagreements.model.sla;

import java.util.ArrayList;
import java.util.List;

import org.processmining.servicelevelagreements.model.ProcessEntity;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;

public class ServiceLevelAgreementTemplate implements IServiceLevelAgreementTemplate {

	private String name;
	private List<String> authors;
	private String description;
	private ProcessEntity processEntity;
	private List<ServiceLevelAgreementTemplateParameter> parameters;
	private List<String> rules;

	public ServiceLevelAgreementTemplate() {
		setAuthors(new ArrayList<String>());
		setParameters(new ArrayList<ServiceLevelAgreementTemplateParameter>());
		setRules(new ArrayList<String>());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ProcessEntity getProcessEntity() {
		return processEntity;
	}

	public void setProcessEntity(ProcessEntity processEntity) {
		this.processEntity = processEntity;
	}

	public List<ServiceLevelAgreementTemplateParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ServiceLevelAgreementTemplateParameter> parameters) {
		this.parameters = parameters;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public String toString() {
		return name;
	}

}
