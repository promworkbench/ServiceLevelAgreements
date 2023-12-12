package org.processmining.servicelevelagreements.model.xml;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.servicelevelagreements.model.ProcessEntity;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplates;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameterType;

/**
 * Parser that parses an {@link org.dom4j.Document} containing SLA Templates to
 * an {@link ServiceLevelAgreementTemplates} object.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class ServiceLevelAgreementTemplateParser {

	public static ServiceLevelAgreementTemplates importFromStream(InputStream input) {

		String xsdSchemaLocation = "files/xml/ServiceLevelAgreementTemplateSpecificationSchema.xsd";
		Document doc = XMLXSDMarshaller.importFromStream(input, xsdSchemaLocation);
		return xmlDocumentToConstraintTemplates(doc);

	}

	public static ServiceLevelAgreementTemplates importFromStream(PluginContext context, InputStream input) {

		String xsdSchemaLocation = "files/xml/ServiceLevelAgreementTemplateSpecificationSchema.xsd";
		Document doc = XMLXSDMarshaller.importFromStream(context, input, xsdSchemaLocation);
		return xmlDocumentToConstraintTemplates(doc);

	}

	/**
	 * Parses the XML Document into a ConstraintTemplates object. It is assumed
	 * the document is valid.
	 * 
	 * @param document
	 *            The document to parse.
	 * @return The constraint templates in the document.
	 */
	private static ServiceLevelAgreementTemplates xmlDocumentToConstraintTemplates(Document document) {
		ServiceLevelAgreementTemplates constraintTemplates = new ServiceLevelAgreementTemplates();
		List<Node> nodes = document.selectNodes("sla:templates/template");
		for (Node constraintTemplateNode : nodes) {
			ServiceLevelAgreementTemplate constraintTemplate = new ServiceLevelAgreementTemplate();
			constraintTemplate.setName(constraintTemplateNode.selectSingleNode("name").getText());
			for (Node authorNode : constraintTemplateNode.selectNodes("author")) {
				constraintTemplate.getAuthors().add(authorNode.getText());
			}
			constraintTemplate.setDescription(constraintTemplateNode.selectSingleNode("description").getText());
			constraintTemplate.setProcessEntity(
					ProcessEntity.parseFromText(constraintTemplateNode.selectSingleNode("processEntity").getText()));
			for (Node parameterNode : constraintTemplateNode.selectNodes("parameter")) {
				ServiceLevelAgreementTemplateParameter constraintTemplateParameter = new ServiceLevelAgreementTemplateParameter();
				constraintTemplateParameter.setKey(parameterNode.selectSingleNode("key").getText());
				constraintTemplateParameter.setType(ServiceLevelAgreementTemplateParameterType
						.parseFromText(parameterNode.selectSingleNode("type").getText()));
				constraintTemplateParameter.setDescription(parameterNode.selectSingleNode("description").getText());
				constraintTemplate.getParameters().add(constraintTemplateParameter);
			}
			for (Node ruleNode : constraintTemplateNode.selectNodes("rule")) {
				constraintTemplate.getRules().add(ruleNode.getText());
			}
			constraintTemplates.add(constraintTemplate);
		}
		return constraintTemplates;
	}
}
