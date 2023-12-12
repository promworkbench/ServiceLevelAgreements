package org.processmining.servicelevelagreements.dialog;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.TextStep;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;

public class ServiceLevelAgreementsWizard {

	public static ServiceLevelAgreementsParameters show(UIPluginContext context, XLog eventlog,
			ServiceLevelAgreementsParameters parameters) {

		@SuppressWarnings("unchecked")
		ListWizard<ServiceLevelAgreementsParameters> wizard = new ListWizard<ServiceLevelAgreementsParameters>(
				new TextStep<ServiceLevelAgreementsParameters>("Service Level Agreement Constraints",
						"<html>" + "<p>In the following screens you can set up the service level agreements based on the provided templates."),
				new ServiceLevelAgreementsParametersStep("Service Level Agreement Constraints", parameters, eventlog));

		return ProMWizardDisplay.show(context, wizard, parameters);
	}

}
