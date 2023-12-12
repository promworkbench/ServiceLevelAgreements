package org.processmining.servicelevelagreements.plugin.inout;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.servicelevelagreements.help.ServiceLevelAgreementsTemplatesInputPluginHelp;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplates;
import org.processmining.servicelevelagreements.model.xml.ServiceLevelAgreementTemplateParser;

@Plugin(
		name = "Service Level Agreements Templates",
		parameterLabels = { "Filename" },
		returnLabels = { "Service Level Agreements Templates" },
		returnTypes = { ServiceLevelAgreementTemplates.class },
		help = ServiceLevelAgreementsTemplatesInputPluginHelp.HELP,
		keywords = { "service level agreements", "sla", "templates", "xml", "input", "import" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Good,
		level = PluginLevel.NightlyBuild)
@UIImportPlugin(
		description = "Service Level Agreements Templates",
		extensions = { "xml" })
public class ServiceLevelAgreementsTemplatesInputPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("Service Level Agreements Templates", "xml");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes) {
		return ServiceLevelAgreementTemplateParser.importFromStream(context, input);
	}

}
