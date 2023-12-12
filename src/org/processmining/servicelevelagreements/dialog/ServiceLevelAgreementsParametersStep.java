package org.processmining.servicelevelagreements.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMScrollContainer;
import org.processmining.framework.util.ui.widgets.ProMScrollContainerChild;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreement;
import org.processmining.servicelevelagreements.model.sla.ServiceLevelAgreementTemplate;
import org.processmining.servicelevelagreements.model.xes.classification.XEventCaseClassifier;
import org.processmining.servicelevelagreements.model.xes.classification.XEventInstanceClassifier;
import org.processmining.servicelevelagreements.parameter.ServiceLevelAgreementsParameters;
import org.processmining.servicelevelagreements.parameter.servicelevelagreement.ServiceLevelAgreementTemplateParameter;
import org.processmining.servicelevelagreements.util.SpringUtilities;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Strings;

public class ServiceLevelAgreementsParametersStep extends ProMHeaderPanel
		implements ProMWizardStep<ServiceLevelAgreementsParameters> {

	private static final long serialVersionUID = 1054786127714893932L;

	private String title;

	private JPanel pnlAddFunctions;
	private ProMComboBox<ServiceLevelAgreementTemplate> cmbAvailableSLATemplates;
	private JButton btnAddSLA;
	private ProMScrollContainer lstSelectedSLATemplates;

	public ServiceLevelAgreementsParametersStep(String title, final ServiceLevelAgreementsParameters parameters,
			final XLog eventlog) {
		super(title);
		setTitle(title);

		// Panel that shows all available sla templates in a combobox and the button to add them to the selection.
		pnlAddFunctions = new JPanel();
		pnlAddFunctions.setLayout(new BoxLayout(pnlAddFunctions, BoxLayout.X_AXIS));

		// Sort the sla templates by their title
		Comparator<ServiceLevelAgreementTemplate> slaTemplateComparator = new Comparator<ServiceLevelAgreementTemplate>() {
			public int compare(ServiceLevelAgreementTemplate c1, ServiceLevelAgreementTemplate c2) {
				return c1.getName().compareTo(c2.getName());
			}
		};
		Collections.sort(parameters.getAvailableSLATemplates(), slaTemplateComparator);
		cmbAvailableSLATemplates = new ProMComboBox<ServiceLevelAgreementTemplate>(
				parameters.getAvailableSLATemplates());
		pnlAddFunctions.add(cmbAvailableSLATemplates);

		btnAddSLA = SlickerFactory.instance().createButton("+");
		btnAddSLA.setToolTipText("Add an SLA constraint of this template.");
		pnlAddFunctions.add(btnAddSLA);

		// Panel (ProMScrollContainer) that shows all selected performance functions and their settings. 
		lstSelectedSLATemplates = new ProMScrollContainer();

		btnAddSLA.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// Add the selected SLA to the list
				// Note that only a panel (ServiceLevelAgreementProMTitledScrollContainerChild) is added, that keeps in it as a field the SLA.
				// These SLA objects are retrieved and added to the parameters object in the apply method.

				// Find which SLA template is selected to be added.
				ServiceLevelAgreementTemplate slaTemplate = (ServiceLevelAgreementTemplate) cmbAvailableSLATemplates
						.getSelectedItem();
				// Create an instance of the SLA of the selected template.
				ServiceLevelAgreement sla = new ServiceLevelAgreement();
				sla.setTemplate(slaTemplate);
				// Create the GUI for setting the parameters for the SLA.
				ServiceLevelAgreementProMTitledScrollContainerChild slaPanel = getGUIPanel(lstSelectedSLATemplates, sla,
						eventlog);
				// Show the GUI.
				lstSelectedSLATemplates.addChild(slaPanel);

			}

		});

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, 550));
		this.add(pnlAddFunctions);
		this.add(lstSelectedSLATemplates);
	}

	public boolean canApply(ServiceLevelAgreementsParameters parameters, JComponent component) {
		return true;
	}

	public ServiceLevelAgreementsParameters apply(ServiceLevelAgreementsParameters parameters, JComponent component) {

		parameters.getServiceLevelAgreements().clear();

		// Save all set parameters back to the parameters object.
		// Loop over the scroll container and add all children to the parameters object
		for (ProMScrollContainerChild child : lstSelectedSLATemplates.getChildren()) {
			ServiceLevelAgreementProMTitledScrollContainerChild slaChild = (ServiceLevelAgreementProMTitledScrollContainerChild) child;
			parameters.getServiceLevelAgreements().add(slaChild.getServiceLevelAgreement());
		}

		return parameters;
	}

	public JComponent getComponent(ServiceLevelAgreementsParameters parameters) {
		lstSelectedSLATemplates.clearChildren();

		//TODO [low] Show SLAs that are already set (usually these will be the default ones)
		//		for (Constraint constraint : parameters.getConstraints()) {
		//			ProMScrollContainerChild constraintParametersPanel = constraint.getParameters()
		//					.getGUIPanel(lstSelectedConstraints, constraint, null);
		//			lstSelectedConstraints.addChild(constraintParametersPanel);
		//		}

		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ServiceLevelAgreementProMTitledScrollContainerChild getGUIPanel(ProMScrollContainer parent,
			final ServiceLevelAgreement constraint, XLog eventlog) {

		/**
		 * Create all necessary stuff
		 */

		// Activity names from the event log.
		List<String> activityNameList = new ArrayList<String>();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(eventlog, new XEventNameClassifier());
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityNameList.add(eventClass.toString());
		}
		Collections.sort(activityNameList);

		// Activity instance names from the event log.
		List<String> activityInstanceList = new ArrayList<String>();
		logInfo = XLogInfoFactory.createLogInfo(eventlog, new XEventInstanceClassifier());
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			activityInstanceList.add(eventClass.toString());
		}
		Collections.sort(activityInstanceList);

		// Cases from the event log.
		List<String> caseList = new ArrayList<String>();
		logInfo = XLogInfoFactory.createLogInfo(eventlog, new XEventCaseClassifier());
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			caseList.add(eventClass.toString());
		}
		Collections.sort(caseList);

		/**
		 * Create the actual panel
		 */

		ServiceLevelAgreementProMTitledScrollContainerChild panel = new ServiceLevelAgreementProMTitledScrollContainerChild(
				parent, constraint);
		JPanel content = panel.getContentPanel();
		content.setLayout(new SpringLayout());
		int numberOfRows = 0;

		// The description of the template this constraint is based on..
		JLabel lblDescription2 = SlickerFactory.instance()
				.createLabel("<html>" + constraint.getTemplate().getDescription() + "</html>");
		lblDescription2.setText(constraint.getTemplate().getDescription());
		lblDescription2.setToolTipText(constraint.getTemplate().getDescription());
		JLabel lblDescription = SlickerFactory.instance().createLabel("Description:");
		lblDescription.setLabelFor(lblDescription2);
		content.add(lblDescription);
		content.add(lblDescription2);
		numberOfRows++;

		// The title of this constraint instance.
		final ProMTextField txtTitle = new ProMTextField("", "Insert the name of the constraint.");
		txtTitle.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				constraint.setName(txtTitle.getText());
			}
		});
		JLabel lblTitle = SlickerFactory.instance().createLabel("Constraint name:");
		lblTitle.setLabelFor(txtTitle);
		content.add(lblTitle);
		content.add(txtTitle);
		numberOfRows++;

		for (final ServiceLevelAgreementTemplateParameter ctParameter : constraint.getTemplate().getParameters()) {
			JLabel lblCTParameterDescription = SlickerFactory.instance()
					.createLabel(ctParameter.getDescription() + ":");
			switch (ctParameter.getType()) {

				case BOOLEAN :

					final JCheckBox cbBoolean = SlickerFactory.instance().createCheckBox("", false);
					lblCTParameterDescription.setLabelFor(cbBoolean);

					cbBoolean.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							constraint.setParameterValue(ctParameter.getKey(), cbBoolean.isSelected());
						}
					});

					content.add(lblCTParameterDescription);
					content.add(cbBoolean);
					numberOfRows++;

					break;

				case ACTIVITY :

					final ProMComboBox<String> cmb = new ProMComboBox<String>(activityNameList);
					cmb.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								constraint.setParameterValue(ctParameter.getKey(), cmb.getSelectedItem());
							}
						}
					});
					lblCTParameterDescription.setLabelFor(cmb);
					if (activityNameList.size() > 0)
						cmb.setSelectedIndex(0);

					content.add(lblCTParameterDescription);
					content.add(cmb);
					numberOfRows++;

					break;

				case DURATION :

					final ProMTextField txtDuration = new ProMTextField("", "Please enter integer numbers.");
					lblCTParameterDescription.setLabelFor(txtDuration);

					final ProMComboBox<TimeUnit> cmbUnit = new ProMComboBox<TimeUnit>(TimeUnit.values());
					JLabel lblUnit = SlickerFactory.instance().createLabel("Unit:");
					lblUnit.setLabelFor(cmbUnit);
					cmbUnit.setSelectedItem(TimeUnit.MINUTES);

					txtDuration.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtDuration.getText()))
								if (LongValidator.getInstance().isValid(txtDuration.getText())) {
									constraint.setParameterValue(ctParameter.getKey(),
											TimeUnit.MILLISECONDS.convert(Long.parseLong(txtDuration.getText()),
													(TimeUnit) cmbUnit.getSelectedItem()));
								} else {
									// Input is not a valid Long. Show notification
									//TODO [low] show notification
								}
						}
					});
					cmbUnit.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED
									&& !Strings.isNullOrEmpty(txtDuration.getText())) {
								if (LongValidator.getInstance().isValid(txtDuration.getText())) {
									constraint.setParameterValue(ctParameter.getKey(),
											TimeUnit.MILLISECONDS.convert(Long.parseLong(txtDuration.getText()),
													(TimeUnit) cmbUnit.getSelectedItem()));
								} else {
									// Input is not a valid Long. Show notification
									//TODO [low] show notification
								}
							}
						}
					});
					content.add(lblCTParameterDescription);
					content.add(txtDuration);
					content.add(lblUnit);
					content.add(cmbUnit);
					numberOfRows += 2;

					break;

				case DATE :

					final ProMTextField txtDate = new ProMTextField("");
					txtDate.setHint("Please enter date (dd/MM/yyyy).");
					lblCTParameterDescription.setLabelFor(txtDate);

					final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

					txtDate.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtDate.getText()))
								try {
									constraint.setParameterValue(ctParameter.getKey(),
											dateFormatter.parse(txtDate.getText()));
								} catch (ParseException e) {
									e.printStackTrace();
								}
						}
					});

					content.add(lblCTParameterDescription);
					content.add(txtDate);
					numberOfRows++;

					break;

				case DATETIME :

					final ProMTextField txtDateTime = new ProMTextField("");
					txtDateTime.setHint("Please enter time (dd/MM/yyyy HH:mm:ss).");
					lblCTParameterDescription.setLabelFor(txtDateTime);

					final DateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

					txtDateTime.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtDateTime.getText()))
								try {
									constraint.setParameterValue(ctParameter.getKey(),
											dateTimeFormatter.parse(txtDateTime.getText()));
								} catch (ParseException e) {
									e.printStackTrace();
								}
						}
					});

					content.add(lblCTParameterDescription);
					content.add(txtDateTime);
					numberOfRows++;

					break;

				case INTEGER :

					final ProMTextField txtInteger = new ProMTextField("");
					txtInteger.setHint("Please enter integer numbers.");
					lblCTParameterDescription.setLabelFor(txtInteger);

					txtInteger.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtInteger.getText())) {
								if (IntegerValidator.getInstance().isValid(txtInteger.getText())) {
									constraint.setParameterValue(ctParameter.getKey(),
											Integer.parseInt(txtInteger.getText()));
								} else {
									// Input is not a valid Integer. Show notification
									//TODO [low] show notification
								}
							}
						}
					});

					content.add(lblCTParameterDescription);
					content.add(txtInteger);
					numberOfRows++;

					break;

				case DOUBLE :

					final ProMTextField txtDouble = new ProMTextField("");
					txtDouble.setHint("Please enter decimal numbers.");
					lblCTParameterDescription.setLabelFor(txtDouble);

					txtDouble.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtDouble.getText())) {
								if (DoubleValidator.getInstance().isValid(txtDouble.getText())) {
									constraint.setParameterValue(ctParameter.getKey(),
											Double.parseDouble(txtDouble.getText()));
								} else {
									// Input is not a valid Double. Show notification
									//TODO [low] show notification
								}
							}
						}
					});

					content.add(lblCTParameterDescription);
					content.add(txtDouble);
					numberOfRows++;

					break;

				case TEXT :

					final ProMTextField txtText = new ProMTextField("");
					txtText.setHint("Please enter text.");
					lblCTParameterDescription.setLabelFor(txtText);

					txtText.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if (!Strings.isNullOrEmpty(txtText.getText()))
								constraint.setParameterValue(ctParameter.getKey(), txtText.getText());
						}
					});

					content.add(lblCTParameterDescription);
					content.add(txtText);
					numberOfRows++;

					break;

				//TODO [high] Add missing parameter type GUIs
				case ACTIVITYINSTANCE :

					final ProMComboBox<String> cmb2 = new ProMComboBox<String>(activityNameList);
					cmb2.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								constraint.setParameterValue(ctParameter.getKey(), cmb2.getSelectedItem());
							}
						}
					});
					lblCTParameterDescription.setLabelFor(cmb2);
					if (activityNameList.size() > 0)
						cmb2.setSelectedIndex(0);

					content.add(lblCTParameterDescription);
					content.add(cmb2);
					numberOfRows++;

					break;

				case CASE :
					break;
				case RESOURCE :
					break;

				default :
					/**
					 * We've selected a type that is either invalid, or missing
					 * from this class, so we cannot create a GUI item for it.
					 * Normally, this can never happen as we validate the
					 * templates XML against the XSD. Notify the user with a
					 * label just in case it does occur.
					 */
					JLabel lblUnknown = SlickerFactory.instance().createLabel(
							"No GUI can be constructed for parameter type " + ctParameter.getType().toString() + "!");
					lblCTParameterDescription.setLabelFor(lblUnknown);
					numberOfRows += 1;
					break;
			}
		}

		// Arrange all content in a grid of two columns.
		SpringUtilities.makeCompactGrid(content, numberOfRows, 2, //rows, cols
				6, 6, //initX, initY
				6, 6); //xPad, yPad

		// Reserve 35 px height for each parameter.
		content.setPreferredSize(new Dimension(content.getPreferredSize().width, numberOfRows * 35 + 5));

		return panel;

	}
}
