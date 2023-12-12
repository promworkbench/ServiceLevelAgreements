package org.processmining.servicelevelagreements.model.xml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplates;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;

public class ServiceLevelAgreementTemplateComposer {

	public static Document xmlDocumentToConstraintTemplates(ServiceLevelAgreementTemplates slaTemplates) {

		Document document = DocumentHelper.createDocument();

		//TODO [high] create actual SLA namespace URL
		Element root = document.addElement("sla:templates")
				.addNamespace("sla", "http://www.processmining.org/ServiceLevelAgreementTemplateSpecificationSchema")
				.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance").addAttribute("xsi:schemaLocation",
						"http://www.processmining.org/ServiceLevelAgreementTemplateSpecificationSchema ServiceLevelAgreementTemplateSpecificationSchema.xsd ");

		for (ServiceLevelAgreementTemplate slaTemplate : slaTemplates) {
			Element ctElement = root.addElement("template");
			ctElement.addElement("name").addText(slaTemplate.getName());
			for (String author : slaTemplate.getAuthors()) {
				ctElement.addElement("author").addText(author);
			}
			ctElement.addElement("description").addText(slaTemplate.getDescription());
			ctElement.addElement("processEntity").addText(slaTemplate.getProcessEntity().name());
			for (ServiceLevelAgreementTemplateParameter parameter : slaTemplate.getParameters()) {
				Element pElement = ctElement.addElement("parameter");
				pElement.addElement("key").addText(parameter.getKey());
				pElement.addElement("type").addText(parameter.getType().name());
				pElement.addElement("description").addText(parameter.getDescription());
			}
			for (String rule : slaTemplate.getRules()) {
				// We need to add the preserve attribute to maintain linebreaks in the rule definitions.
				ctElement.addElement("rule").addText(rule).addAttribute(QName.get("space", Namespace.XML_NAMESPACE),
						"preserve");

			}
		}

		return document;
	}
}