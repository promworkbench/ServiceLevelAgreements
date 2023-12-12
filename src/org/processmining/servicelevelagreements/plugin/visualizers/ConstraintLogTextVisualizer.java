package org.processmining.servicelevelagreements.plugin.visualizers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.servicelevelagreements.model.ConstraintLog;
import org.processmining.servicelevelagreements.util.CollectionUtils;
import org.processmining.servicelevelagreements.util.ScrollablePanel;

@Plugin(
		name = "Visualize ConstraintLog as text",
		returnLabels = { "Visualize ConstraintLog" },
		returnTypes = { JComponent.class },
		parameterLabels = { "ConstraintLog" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class ConstraintLogTextVisualizer {

	@PluginVariant(
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext uiPluginContext, ConstraintLog constraintLog) {
		long time = -System.currentTimeMillis();
		System.out.println("[ConstraintLogVisualizer] Start.");

		ScrollablePanel panel = new ScrollablePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(panel);

		DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		JLabel text = new JLabel();

		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		for (String constraint : constraintLog.getOutput().keySet()) {
			builder.append("<h1>" + constraint + "</h1>");
			for (String status : CollectionUtils.asSortedList(constraintLog.getOutput().get(constraint).keySet())) {
				builder.append("<h2>" + status + "</h2>");
				for (String argument : CollectionUtils
						.asSortedList(constraintLog.getOutput().get(constraint).get(status).keySet())) {
					builder.append("argument: " + argument + " - ");
					for (Pair<String, String> interval : constraintLog.getOutput().get(constraint).get(status)
							.get(argument)) {
						String start = interval.getFirst();
						String end = interval.getSecond();

						Date startDate = new Date(Long.parseLong(start));
						Date endDate = (end.equals("inf")) ? null : new Date(Long.parseLong(end));

						builder.append(String.format("(%s,%s) ", dateFormatter.format(startDate),
								(endDate == null) ? "open" : dateFormatter.format(endDate)));
					}
					builder.append("<br/>");
				}
			}
		}
		builder.append("</html>");

		text.setText(builder.toString());
		panel.add(text);

		time += System.currentTimeMillis();
		System.out.println("[ConstraintLogVisualizer] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");
		return scroll;

	}
}
