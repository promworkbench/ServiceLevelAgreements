package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Calendar;
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
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.servicelevelagreements.model.ConstraintLog;
import org.processmining.servicelevelagreements.util.ScrollablePanel;

@Plugin(
		name = "Visualize ConstraintLog as Gantt chart (DEMO)",
		returnLabels = { "Visualize ConstraintLog" },
		returnTypes = { JComponent.class },
		parameterLabels = { "ConstraintLog" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class ConstraintLogGanttChartVisualizerDemo {

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

		final Task t1 = new Task("SLA 1", date(1, Calendar.APRIL, 2001), date(5, Calendar.APRIL, 2001));
		t1.setPercentComplete(1.00);
		s1.add(t1);

		final Task t2 = new Task("SLA2", date(9, Calendar.APRIL, 2001), date(9, Calendar.APRIL, 2001));
		t2.setPercentComplete(1.00);
		s1.add(t2);

		// here is a task split into two subtasks...
		final Task t3 = new Task("SLA 3", date(10, Calendar.APRIL, 2001), date(5, Calendar.MAY, 2001));
		final Task st31 = new Task("SLA 3.1", date(10, Calendar.APRIL, 2001), date(25, Calendar.APRIL, 2001));
		st31.setPercentComplete(1.0);
		final Task st32 = new Task("SLA 3.2", date(1, Calendar.MAY, 2001), date(5, Calendar.MAY, 2001));
		st32.setPercentComplete(1.0);
		t3.addSubtask(st31);
		t3.addSubtask(st32);
		s1.add(t3);

		// and another...
		final Task t4 = new Task("SLA 44", date(6, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		final Task st41 = new Task("SLA 4.1", date(6, Calendar.MAY, 2001), date(10, Calendar.MAY, 2001));
		st41.setPercentComplete(1.0);
		final Task st42 = new Task("SLA 4.2", date(15, Calendar.MAY, 2001), date(20, Calendar.MAY, 2001));
		st42.setPercentComplete(1.0);
		final Task st43 = new Task("SLA 4.3", date(23, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		st43.setPercentComplete(0.50);
		t4.addSubtask(st41);
		t4.addSubtask(st42);
		t4.addSubtask(st43);
		s1.add(t4);

		final Task t5 = new Task("SLA 5", date(2, Calendar.JUNE, 2001), date(2, Calendar.JUNE, 2001));
		s1.add(t5);

		final Task t6 = new Task("SLA 6", date(3, Calendar.JUNE, 2001), date(31, Calendar.JULY, 2001));
		t6.setPercentComplete(0.60);

		s1.add(t6);

		final Task t7 = new Task("SLA 7", date(1, Calendar.AUGUST, 2001), date(8, Calendar.AUGUST, 2001));
		t7.setPercentComplete(0.0);
		s1.add(t7);

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		return collection;
	}

	/**
	 * Utility method for creating <code>Date</code> objects.
	 *
	 * @param day
	 *            the date.
	 * @param month
	 *            the month.
	 * @param year
	 *            the year.
	 *
	 * @return a date.
	 */
	private static Date date(final int day, final int month, final int year) {

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		final Date result = calendar.getTime();
		return result;

	}
}
