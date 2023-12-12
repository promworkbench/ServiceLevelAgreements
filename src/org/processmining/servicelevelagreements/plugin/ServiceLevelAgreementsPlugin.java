package org.processmining.servicelevelagreements.plugin;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.servicelevelagreements.algorithm.impl.BasicServiceLevelAgreementsAlgorithm;
import org.processmining.servicelevelagreements.constant.AuthorConstants;
import org.processmining.servicelevelagreements.dialog.ServiceLevelAgreementsWizard;
import org.processmining.servicelevelagreements.help.ServiceLevelAgreementsPluginHelp;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplates;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;

@Plugin(
		name = "Service Level Agreements",
		parameterLabels = { "Event log", "SLA templates", "SLA parameters" },
		returnLabels = { "EventDatabase", "SLA parameters" },
		returnTypes = { EventDatabase.class, ServiceLevelAgreementsParameters.class },
		mostSignificantResult = 1,
		help = ServiceLevelAgreementsPluginHelp.HELP,
		keywords = { "service level agreements", "slas", "constraint checking", "performance" },
		categories = { PluginCategory.Analytics, PluginCategory.ConformanceChecking },
		handlesCancel = true,
		userAccessible = true,
		level = PluginLevel.NightlyBuild,
		quality = PluginQuality.Fair)
public class ServiceLevelAgreementsPlugin extends BasicServiceLevelAgreementsAlgorithm {

	@UITopiaVariant(
			author = AuthorConstants.NAME_BART,
			email = AuthorConstants.EMAIL_BART,
			affiliation = AuthorConstants.AFFILIATION_TUE,
			uiHelp = ServiceLevelAgreementsPluginHelp.DEFAULTVARIANT,
			uiLabel = "Service Level Agreements, default")
	@PluginVariant(
			variantLabel = "Service Level Agreements, default",
			requiredParameterLabels = { 0 },
			help = ServiceLevelAgreementsPluginHelp.DEFAULTVARIANT)
	public Object[] runDefault(PluginContext pluginContext, XLog xlog) {
		ServiceLevelAgreementsParameters parameters = new ServiceLevelAgreementsParameters();
		return new Object[] { runPrivate(pluginContext, xlog, parameters), parameters };
	}

	@UITopiaVariant(
			author = AuthorConstants.NAME_BART,
			email = AuthorConstants.EMAIL_BART,
			affiliation = AuthorConstants.AFFILIATION_TUE,
			uiHelp = ServiceLevelAgreementsPluginHelp.SLATEMPLATESVARIANT,
			uiLabel = "Service Level Agreements, constraint templates")
	@PluginVariant(
			variantLabel = "Service Level Agreements, constraint templates",
			requiredParameterLabels = { 0, 1 },
			help = ServiceLevelAgreementsPluginHelp.SLATEMPLATESVARIANT)
	public Object[] runDefault(PluginContext pluginContext, XLog xlog,
			ServiceLevelAgreementTemplates constraintTemplates) {
		ServiceLevelAgreementsParameters parameters = new ServiceLevelAgreementsParameters();
		parameters.getAvailableSLATemplates().addAll(constraintTemplates);
		return new Object[] { runPrivate(pluginContext, xlog, parameters), parameters };
	}

	@UITopiaVariant(
			author = AuthorConstants.NAME_BART,
			email = AuthorConstants.EMAIL_BART,
			affiliation = AuthorConstants.AFFILIATION_TUE,
			uiHelp = ServiceLevelAgreementsPluginHelp.SLAPARAMETERSVARIANT,
			uiLabel = "Service Level Agreements, sla parameters")
	@PluginVariant(
			variantLabel = "Service Level Agreements, sla parameters",
			requiredParameterLabels = { 0, 2 },
			help = ServiceLevelAgreementsPluginHelp.SLAPARAMETERSVARIANT)
	public Object[] runDefault(PluginContext pluginContext, XLog xlog, ServiceLevelAgreementsParameters parameters) {
		return new Object[] { runPrivate(pluginContext, xlog, parameters), parameters };
	}

	@UITopiaVariant(
			author = AuthorConstants.NAME_BART,
			email = AuthorConstants.EMAIL_BART,
			affiliation = AuthorConstants.AFFILIATION_TUE,
			uiHelp = ServiceLevelAgreementsPluginHelp.DIALOGVARIANT,
			uiLabel = "Service Level Agreements, dialog")
	@PluginVariant(
			variantLabel = "Service Level Agreements, dialog",
			requiredParameterLabels = { 0 },
			help = ServiceLevelAgreementsPluginHelp.DIALOGVARIANT)
	public Object[] runDialog(UIPluginContext uiPluginContext, XLog xlog) {
		ServiceLevelAgreementsParameters parameters = new ServiceLevelAgreementsParameters();
		parameters = ServiceLevelAgreementsWizard.show(uiPluginContext, xlog, parameters);
		if (parameters == null) {
			// The dialog was cancelled
			uiPluginContext.getFutureResult(0).cancel(true);
			return null;
		}
		return new Object[] { runPrivate(uiPluginContext, xlog, parameters), parameters };
	}

	@UITopiaVariant(
			author = AuthorConstants.NAME_BART,
			email = AuthorConstants.EMAIL_BART,
			affiliation = AuthorConstants.AFFILIATION_TUE,
			uiHelp = ServiceLevelAgreementsPluginHelp.DIALOGSLATEMPLATESVARIANT,
			uiLabel = "Service Level Agreements, dialog, constraint templates")
	@PluginVariant(
			variantLabel = "Service Level Agreements, dialog, constraint templates",
			requiredParameterLabels = { 0, 1 },
			help = ServiceLevelAgreementsPluginHelp.DIALOGSLATEMPLATESVARIANT)
	public Object[] runDialog(UIPluginContext uiPluginContext, XLog xlog,
			ServiceLevelAgreementTemplates constraintTemplates) {
		ServiceLevelAgreementsParameters parameters = new ServiceLevelAgreementsParameters();
		parameters.getAvailableSLATemplates().addAll(constraintTemplates);
		parameters = ServiceLevelAgreementsWizard.show(uiPluginContext, xlog, parameters);
		if (parameters == null) {
			// The dialog was cancelled
			uiPluginContext.getFutureResult(0).cancel(true);
			return null;
		}
		return new Object[] { runPrivate(uiPluginContext, xlog, parameters), parameters };
	}

	//TODO [low] create variant that takes parameter as input and fills GUI based on it
	//	@UITopiaVariant(
	//			author = AuthorConstants.NAME_BART,
	//			email = AuthorConstants.EMAIL_BART,
	//			affiliation = AuthorConstants.AFFILIATION_TUE,
	//			uiHelp = ServiceLevelAgreementsPluginHelp.DIALOGSLAPARAMETERSVARIANT,
	//			uiLabel = "Service Level Agreements, dialog, sla parameters")
	//	@PluginVariant(
	//			variantLabel = "Service Level Agreements, dialog, sla parameters",
	//			requiredParameterLabels = { 0, 2 },
	//			help = ServiceLevelAgreementsPluginHelp.DIALOGSLAPARAMETERSVARIANT)
	//	public Object[] runDialog(UIPluginContext uiPluginContext, XLog xlog, ServiceLevelAgreementsParameters parameters) {
	//		parameters = ServiceLevelAgreementsWizard.show(uiPluginContext, xlog, parameters);
	//		if (parameters == null) {
	//			// The dialog was cancelled
	//			uiPluginContext.getFutureResult(0).cancel(true);
	//			return null;
	//		}
	//		return new Object[] { runPrivate(uiPluginContext, xlog, parameters), parameters };
	//	}

	private EventDatabase runPrivate(PluginContext pluginContext, XLog xlog,
			ServiceLevelAgreementsParameters parameters) {

		long time = -System.currentTimeMillis();
		parameters.displayMessage("[Algorithm] Start");
		parameters.displayMessage("[Algorithm] Parameters: " + parameters.toString());

		EventDatabase eventDatabase = apply(xlog, parameters);

		time += System.currentTimeMillis();
		parameters.displayMessage("[Algorithm] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");

		return eventDatabase;
	}

}
