package org.processmining.servicelevelagreements.plugin.inout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.dom4j.Document;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.servicelevelagreements.help.ServiceLevelAgreementsTemplatesOutputPluginHelp;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplates;
import org.processmining.servicelevelagreements.model.xml.ServiceLevelAgreementTemplateComposer;
import org.processmining.servicelevelagreements.model.xml.XMLXSDUnmarshaller;

@Plugin(
		name = "Export Service Level Agreements Templates",
		returnLabels = {},
		returnTypes = {},
		parameterLabels = { "XML", "File" },
		help = ServiceLevelAgreementsTemplatesOutputPluginHelp.HELP,
		keywords = { "service level agreements", "sla", "templates", "xml", "output", "export" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Good,
		level = PluginLevel.NightlyBuild)
@UIExportPlugin(
		description = "Service Level Agreements Templates",
		extension = "xml")
public class ServiceLevelAgreementsTemplatesOutputPlugin {

	@PluginVariant(
			variantLabel = "Export, default xml unmarshalling",
			requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, ServiceLevelAgreementTemplates constraintTemplates, File file)
			throws FileNotFoundException {

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		Document document = ServiceLevelAgreementTemplateComposer.xmlDocumentToConstraintTemplates(constraintTemplates);
		XMLXSDUnmarshaller.exportToStream(context, document, fileOutputStream);

	}
}
