package org.processmining.servicelevelagreements.help;

public class ServiceLevelAgreementsPluginHelp {

	public final static String HELP = "Checks Service Level Agreements on event log.";
	public final static String DEFAULTVARIANT = HELP
			+ " Loads the default SLA templates, but no SLAs. Doesn't really do anything.";
	public final static String SLATEMPLATESVARIANT = HELP
			+ " Loads the default SLA templates and extends them with the provided templates. No SLAs. Doesn't really do anything.";
	public final static String SLAPARAMETERSVARIANT = HELP
			+ " Loads SLA templates and SLAs from provided SLA parameters.";
	public final static String DIALOGVARIANT = HELP
			+ " Loads the default SLA templates and let's the user set SLAs in the dialog.";
	public final static String DIALOGSLATEMPLATESVARIANT = HELP
			+ " Loads the default SLA templates and extends them with the provided templates. SLAs can be set in the dialog.";
	public final static String DIALOGSLAPARAMETERSVARIANT = HELP
			+ " Loads SLA templates and SLAs from the provided SLA parameters, after which the SLAs can be modified in the dialog.";

}
