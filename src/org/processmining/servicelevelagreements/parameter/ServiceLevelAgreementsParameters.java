package org.processmining.servicelevelagreements.parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.xml.ServiceLevelAgreementTemplateParser;

import com.google.common.base.Objects;

public class ServiceLevelAgreementsParameters extends PluginParametersImpl implements Serializable {

	// FIELDS

	private static final long serialVersionUID = 3778404438397396780L;

	private boolean cloneEventLog;
	private List<ServiceLevelAgreementTemplate> availableSLATemplates;
	private List<ServiceLevelAgreement> serviceLevelAgreements;

	// CONSTRUCTOR

	public ServiceLevelAgreementsParameters() {
		// By default, load the default constraint templates.
		this(true);
	}

	/**
	 * @param loadDefaultConstraintTemplates
	 *            Loads the default constraint templates from XML if set to
	 *            true. Creates empty ArrayList if set to false.
	 */
	public ServiceLevelAgreementsParameters(boolean loadDefaultConstraintTemplates) {
		setCloneEventLog(true);
		if (loadDefaultConstraintTemplates) {
			// Load the default constraint templates XML file
			FileInputStream is;
			try {
				is = new FileInputStream(new File("files/xml/DefaultServiceLevelAgreementTemplateSpecification.xml"));

				// Validate (by loading the XSD) and parse the default constraint templates
				ServiceLevelAgreementTemplateParser a = new ServiceLevelAgreementTemplateParser();
				setAvailableSLATemplates(a.importFromStream(is));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			setAvailableSLATemplates(new ArrayList<ServiceLevelAgreementTemplate>());
		}
		setServiceLevelAgreements(new ArrayList<ServiceLevelAgreement>());
	}

	// GETTERS AND SETTERS

	public boolean isCloneEventLog() {
		return cloneEventLog;
	}

	public void setCloneEventLog(boolean cloneEventLog) {
		this.cloneEventLog = cloneEventLog;
	}

	public List<ServiceLevelAgreementTemplate> getAvailableSLATemplates() {
		return availableSLATemplates;
	}

	public void setAvailableSLATemplates(List<ServiceLevelAgreementTemplate> availableSLATemplates) {
		this.availableSLATemplates = availableSLATemplates;
	}

	public List<ServiceLevelAgreement> getServiceLevelAgreements() {
		return serviceLevelAgreements;
	}

	public void setServiceLevelAgreements(List<ServiceLevelAgreement> serviceLevelAgreements) {
		this.serviceLevelAgreements = serviceLevelAgreements;
	}

	// METHODS

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("SLAs: ");
		for (ServiceLevelAgreement sla : serviceLevelAgreements) {
			builder.append(sla.getTemplate().getName());
			builder.append(" - ");
			builder.append(sla.getName());
		}

		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServiceLevelAgreementsParameters))
			return false;

		ServiceLevelAgreementsParameters param = (ServiceLevelAgreementsParameters) obj;

		if (cloneEventLog != param.isCloneEventLog())
			return false;

		if (!availableSLATemplates.equals(param.getAvailableSLATemplates()))
			return false;

		if (!serviceLevelAgreements.equals(param.getServiceLevelAgreements()))
			return false;

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), cloneEventLog, availableSLATemplates, serviceLevelAgreements);
	}

}
