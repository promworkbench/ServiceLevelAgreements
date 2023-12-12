package org.processmining.servicelevelagreements.model.sla;

import java.util.List;

import org.processmining.servicelevelagreements.model.ProcessEntity;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;

public interface IServiceLevelAgreementTemplate {

	String getName();

	void setName(String name);

	List<String> getAuthors();

	void setAuthors(List<String> authors);

	String getDescription();

	void setDescription(String description);

	ProcessEntity getProcessEntity();

	void setProcessEntity(ProcessEntity processEntity);

	List<ServiceLevelAgreementTemplateParameter> getParameters();

	void setParameters(List<ServiceLevelAgreementTemplateParameter> parameters);

	List<String> getRules();

	void setRules(List<String> rules);

}
