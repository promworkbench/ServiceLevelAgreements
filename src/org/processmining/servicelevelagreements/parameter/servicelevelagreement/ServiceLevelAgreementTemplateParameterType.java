package org.processmining.servicelevelagreements.parameter.servicelevelagreement;

/**
 * Available parameter types for SLA templates. These parameter types are
 * relfected in the SLA templates XML Schema XSD
 * {@link files/ServiceLevelAgreementTemplateSpecificationSchema.xsd}.
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public enum ServiceLevelAgreementTemplateParameterType {
	BOOLEAN, TEXT, INTEGER, DOUBLE, DURATION, DATE, DATETIME, ACTIVITYINSTANCE, ACTIVITY, CASE, RESOURCE;

	ServiceLevelAgreementTemplateParameterType() {

	}

	public static ServiceLevelAgreementTemplateParameterType parseFromText(String text) {

		switch (text) {
			case "Boolean" :
				return BOOLEAN;
			case "Text" :
				return TEXT;
			case "Integer" :
				return INTEGER;
			case "Double" :
				return DOUBLE;
			case "Duration" :
				return DURATION;
			case "Date" :
				return DATE;
			case "DateTime" :
				return DATETIME;
			case "ActvityInstance" :
				return ACTIVITYINSTANCE;
			case "Activity" :
				return ACTIVITY;
			case "Case" :
				return CASE;
			case "Resource" :
				return RESOURCE;
		}

		return null;
	}
}
