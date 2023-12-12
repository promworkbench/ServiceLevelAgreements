package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Color;
import java.awt.Dimension;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.interval.Interval;
import org.processmining.servicelevelagreements.model.interval.IntervalList;
import org.processmining.servicelevelagreements.util.ScrollablePanel;

import com.google.common.collect.Table;

@Plugin(
		name = "Visualize EventDatabase as Gantt chart",
		returnLabels = { "Visualize EventDatabase" },
		returnTypes = { JComponent.class },
		parameterLabels = { "Service Level Agreements" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class EventDatabaseGanttChartVisualizer {

	int tasksToShow = 100;

	@PluginVariant(
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext uiPluginContext, EventDatabase eventDatabase) {
		long time = -System.currentTimeMillis();
		System.out.println("[EventDatabaseGanttChartVisualizer] Start.");

		ScrollablePanel panel = new ScrollablePanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(panel);

		final GanttCategoryDataset dataset = createDataset(eventDatabase);

		//		SlidingGanttCategoryDataset slidingDataset = new SlidingGanttCategoryDataset(dataset, 0, 20);

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
		renderer.setBaseItemLabelGenerator(new IntervalCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseItemLabelPaint(Color.BLACK);
		renderer.setBasePositiveItemLabelPosition(
				new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER));

		// add the chart to a panel...
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1000, 200));
		panel.add(chartPanel);

		time += System.currentTimeMillis();
		System.out.println(
				"[EventDatabaseGanttChartVisualizer] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");
		return scroll;
	}

	private GanttCategoryDataset createDataset(EventDatabase eventDatabase) {

		final TaskSeriesCollection collection = new TaskSeriesCollection();

		Map<String, Table<String, String, IntervalList>> fluentArgumentValueMVIs = eventDatabase.getFluentValueMVIs();
		//		@SuppressWarnings("deprecation")
		//		long today = new Date(2020 - 1900, 1, 1).getTime();
		long lastTimeStamp = eventDatabase.getLastTimeStamp();

		int tasksShown = 0;

		for (String fluent : fluentArgumentValueMVIs.keySet()) {

			TaskSeries series = new TaskSeries(fluent);

			Table<String, String, IntervalList> argumentValueMVIs = fluentArgumentValueMVIs.get(fluent);

			for (String arguments : argumentValueMVIs.rowKeySet()) {

				Set<Task> subtasks = new HashSet<Task>();
				Date startDate = null;
				Date endDate = null;

				for (String value : argumentValueMVIs.row(arguments).keySet()) {

					IntervalList MVI = argumentValueMVIs.get(arguments, value);

					for (Interval I : MVI) {

						if (!I.isEmpty()) {
							long startTimeStamp = I.getStartTimestamp();

							long endTimeStamp;
							if (!I.isOpenEnded()) {
								endTimeStamp = I.getEndTimestamp();
							} else {
								endTimeStamp = lastTimeStamp;
							}

							Task subtask = new Task(value, new Date(startTimeStamp), new Date(endTimeStamp));
							subtasks.add(subtask);

							if (startDate == null || startTimeStamp < startDate.getTime()) {
								startDate = new Date(startTimeStamp);
							}
							if (endDate == null || endTimeStamp > endDate.getTime()) {
								endDate = new Date(endTimeStamp);
							}

						}

					}

				}

				Task task = new Task(arguments, startDate, endDate);
				for (Task subtask : subtasks)
					task.addSubtask(subtask);
				series.add(task);

				tasksShown++;
				if (tasksShown > tasksToShow)
					break;

			}

			collection.add(series);

		}

		return collection;

	}

}
