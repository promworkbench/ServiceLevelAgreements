package org.processmining.servicelevelagreements.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.xml.ServiceLevelAgreementTemplateParser;

public class XMLTest {

	public static void main(String[] args) {
		try {
			// Load the default constraint templates XML file
			FileInputStream is = new FileInputStream(
					new File("files/xml/DefaultServiceLevelAgreementTemplateSpecification.xml"));

			// Validate (by loading the XSD) and parse the default constraint templates
			ServiceLevelAgreementTemplateParser a = new ServiceLevelAgreementTemplateParser();
			List<ServiceLevelAgreementTemplate> ctemplates = a.importFromStream(is);

			System.out.println(ctemplates.size() + " templates found in XML.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
