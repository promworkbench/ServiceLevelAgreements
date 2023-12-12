package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
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
		name = "Visualize ConstraintLog as Gantt chart",
		returnLabels = { "Visualize ConstraintLog" },
		returnTypes = { JComponent.class },
		parameterLabels = { "ConstraintLog" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class ConstraintLogGanttChartVisualizer {

	@PluginVariant(
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext uiPluginContext, ConstraintLog constraintLog) {
		long time = -System.currentTimeMillis();
		System.out.println("[ConstraintLogVisualizer] Start.");

		ScrollablePanel panel = new ScrollablePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(panel);

		final IntervalCategoryDataset dataset = createDataset(constraintLog);

		// create the chart...
		final JFreeChart chart = ChartFactory.createGanttChart("Service Level Agreements", // chart title
				"Constraint", // domain axis label
				"Date", // range axis label
				dataset, // data
				true, // include legend
				true, // tooltips
				false // urls
		);
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		//      plot.getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, Color.blue);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		chartPanel.setPreferredSize(new Dimension(width - 200, height - 200));
		panel.add(chartPanel);

		time += System.currentTimeMillis();
		System.out.println("[ConstraintLogVisualizer] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");
		return scroll;

	}

	private IntervalCategoryDataset createDataset(ConstraintLog constraintLog) {

		final TaskSeries s1 = new TaskSeries("Service Level Agreements");

		for (String constraint : constraintLog.getOutput().keySet()) {

			Task tConstraint = new Task(constraint, null);
			Date constraintStartDate = new Date(Long.MAX_VALUE);
			Date constraintEndDate = new Date(Long.MIN_VALUE);

			for (String status : CollectionUtils.asSortedList(constraintLog.getOutput().get(constraint).keySet())) {

				Task tStatus = new Task(status, null);
				Date statusStartDate = new Date(Long.MAX_VALUE);
				Date statusEndDate = new Date(Long.MIN_VALUE);

				switch (status) {
					case "pending" :
						tStatus.setPercentComplete(0.0d);
						break;
					case "satisfied" :
						tStatus.setPercentComplete(1.0d);
						break;
					case "violated" :
						tStatus.setPercentComplete(0.0d);
						break;
				}

				for (String argument : CollectionUtils
						.asSortedList(constraintLog.getOutput().get(constraint).get(status).keySet())) {

					Task tArgument = new Task(argument, null);
					Date argumentStartDate = new Date(Long.MAX_VALUE);
					Date argumentEndDate = new Date(Long.MIN_VALUE);

					for (Pair<String, String> interval : constraintLog.getOutput().get(constraint).get(status)
							.get(argument)) {
						String start = interval.getFirst();
						String end = interval.getSecond();

						Date intervalStartDate = new Date(Long.parseLong(start));
						Date intervalEndDate = (end.equals("inf")) ? new Date() : new Date(Long.parseLong(end));

						Task tInterval = new Task("interval", intervalStartDate, intervalEndDate);
						tArgument.addSubtask(tInterval);

						if (intervalStartDate.before(argumentStartDate))
							argumentStartDate = intervalStartDate;
						if (intervalEndDate.after(argumentEndDate))
							argumentEndDate = intervalEndDate;

						if (intervalStartDate.before(statusStartDate))
							statusStartDate = intervalStartDate;
						if (intervalEndDate.after(statusEndDate))
							statusEndDate = intervalEndDate;

						if (intervalStartDate.before(constraintStartDate))
							constraintStartDate = intervalStartDate;
						if (intervalEndDate.after(constraintEndDate))
							constraintEndDate = intervalEndDate;
					}

					tArgument.setDuration(new SimpleTimePeriod(argumentStartDate, argumentEndDate));
					tStatus.addSubtask(tArgument);
				}

				tStatus.setDuration(new SimpleTimePeriod(statusStartDate, statusEndDate));
				tConstraint.addSubtask(tStatus);
			}

			tConstraint.setDuration(new SimpleTimePeriod(constraintStartDate, constraintEndDate));
			s1.add(tConstraint);
		}

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		return collection;
	}
}
