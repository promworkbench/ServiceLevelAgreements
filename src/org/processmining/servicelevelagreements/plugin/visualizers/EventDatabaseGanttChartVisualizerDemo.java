package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Color;
import java.text.DateFormat;
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
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.util.ScrollablePanel;

@Plugin(
		name = "Visualize EventDatabase as Gantt chart (DEMO)",
		returnLabels = { "Visualize EventDatabase" },
		returnTypes = { JComponent.class },
		parameterLabels = { "Service Level Agreements" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class EventDatabaseGanttChartVisualizerDemo {

	@PluginVariant(
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext uiPluginContext, EventDatabase eventDatabase) {
		long time = -System.currentTimeMillis();
		System.out.println("[EventDatabaseGanttChartVisualizerDemo] Start.");

		ScrollablePanel panel = new ScrollablePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(panel);

		final GanttCategoryDataset dataset = createDataset(eventDatabase);

		// create the chart...
		final JFreeChart chart = ChartFactory.createGanttChart("Service Level Agreements", // chart title
				null, // domain axis label
				"Date", // range axis label
				dataset, // data
				false, // include legend
				true, // tooltips
				false // urls
		);
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(Color.WHITE);

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.DARK_GRAY);
		plot.setRangeGridlinePaint(Color.DARK_GRAY);

		ServiceLevelAgreementsGanttRenderer renderer = new ServiceLevelAgreementsGanttRenderer();
		plot.setRenderer(renderer);
		renderer.setBaseToolTipGenerator(
				new TaskSubTaskIntervalCategoryToolTipGenerator("{1} {5}: {3} - {4}", DateFormat.getDateInstance()));
		renderer.setShadowVisible(false);

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		panel.add(chartPanel);

		time += System.currentTimeMillis();
		System.out.println("[EventDatabaseGanttChartVisualizerDemo] End (took "
				+ DurationFormatUtils.formatDurationHMS(time) + ").");
		return chartPanel;
	}

	private GanttCategoryDataset createDataset(EventDatabase eventDatabase) {

		final TaskSeries s1 = new TaskSeries("Service Level Agreements");

		final Task t1 = new Task("SLA1", date(1, Calendar.APRIL, 2001), date(5, Calendar.APRIL, 2001));
		//		t1.setPercentComplete(1.00);
		s1.add(t1);

		final Task t2 = new Task("SLA2", date(9, Calendar.APRIL, 2001), date(9, Calendar.APRIL, 2001));
		//		t2.setPercentComplete(1.00);
		s1.add(t2);

		// here is a task split into two subtasks...
		final Task t3 = new Task("SLA3", date(10, Calendar.APRIL, 2001), date(5, Calendar.MAY, 2001));
		final Task st31 = new Task("violated", date(10, Calendar.APRIL, 2001), date(25, Calendar.APRIL, 2001));
		//		st31.setPercentComplete(1.0);
		final Task st32 = new Task("violated", date(1, Calendar.MAY, 2001), date(5, Calendar.MAY, 2001));
		//		st32.setPercentComplete(1.0);
		t3.addSubtask(st31);
		t3.addSubtask(st32);
		s1.add(t3);

		// and another...
		final Task t4 = new Task("SLA 4", date(6, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		final Task st41 = new Task("pending", date(6, Calendar.MAY, 2001), date(10, Calendar.MAY, 2001));
		//		st41.setPercentComplete(1.0);
		final Task st42 = new Task("satisfied", date(15, Calendar.MAY, 2001), date(20, Calendar.MAY, 2001));
		//		st42.setPercentComplete(1.0);
		final Task st43 = new Task("violated", date(23, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		//		st43.setPercentComplete(0.50);

		t4.addSubtask(st41);
		t4.addSubtask(st42);
		t4.addSubtask(st43);
		s1.add(t4);

		final Task t5 = new Task("SLA 5", date(2, Calendar.JUNE, 2001), date(2, Calendar.JUNE, 2001));
		s1.add(t5);

		final Task t6 = new Task("SLA 6", date(3, Calendar.JUNE, 2001), date(31, Calendar.JULY, 2001));
		//		t6.setPercentComplete(0.60);
		s1.add(t6);

		final Task t7 = new Task("SLA 7", date(1, Calendar.AUGUST, 2001), date(8, Calendar.AUGUST, 2001));
		//		t7.setPercentComplete(0.0);
		s1.add(t7);

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		//	    SlidingGanttCategoryDataset slidingDataset = new SlidingGanttCategoryDataset(collection, 0, 10);

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
