package org.processmining.servicelevelagreements.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.processmining.servicelevelagreements.model.reasoner.Reasoner;
import org.processmining.servicelevelagreements.model.reasoner.impl.BasicDeductiveReasoner;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.xml.ServiceLevelAgreementTemplateParser;

public class ReasonerTest {

	public static void main(String[] args) {
		try {
			// Load the default constraint templates XML file
			FileInputStream is = new FileInputStream(
					new File("files/xml/DefaultServiceLevelAgreementTemplateSpecification.xml"));

			// Validate (by loading the XSD) and parse the default constraint templates
			ServiceLevelAgreementTemplateParser a = new ServiceLevelAgreementTemplateParser();
			List<ServiceLevelAgreementTemplate> ctemplates = a.importFromStream(is);

			ServiceLevelAgreement sla = new ServiceLevelAgreement();
			sla.setTemplate(ctemplates.get(0));
			sla.setName("sla1");
			sla.setParameterValue("fromActivity", "a");
			sla.setParameterValue("toActivity", "b");
			sla.setParameterValue("duration", "123");

			Reasoner r = new BasicDeductiveReasoner();

			r.handleAgreement(sla);

			String test = "[a, b, c,d]";
			test = "[a]";
			System.out.println(test.trim().substring(1, test.length() - 1));
			List<String> testList = Arrays.asList(test.trim().substring(1, test.length() - 1).split("\\s*,\\s*"));
			for (String element : testList) {
				System.out.println("'" + element + "'");
			}

			System.out.println(Arrays.asList(sla.getTemplate().getRules().get(1).split("\\r?\\n")));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
