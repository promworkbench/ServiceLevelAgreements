package org.processmining.servicelevelagreements.dialog;

import org.processmining.framework.util.ui.widgets.ProMScrollContainer;
import org.processmining.framework.util.ui.widgets.ProMTitledScrollContainerChild;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;

public class ServiceLevelAgreementProMTitledScrollContainerChild extends ProMTitledScrollContainerChild {

	private static final long serialVersionUID = -3569294464599403241L;

	private ServiceLevelAgreement sla;

	public ServiceLevelAgreementProMTitledScrollContainerChild(ProMScrollContainer parent, ServiceLevelAgreement sla) {
		super(sla.getTemplate().getName(), parent, true);
		setServiceLevelAgreement(sla);
	}

	public ServiceLevelAgreement getServiceLevelAgreement() {
		return sla;
	}

	public void setServiceLevelAgreement(ServiceLevelAgreement constraint) {
		this.sla = constraint;
	}

}
