package org.processmining.servicelevelagreements.plugin.visualizers;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JComponent;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logprojection.LogView;
import org.processmining.logprojection.plugins.dottedchart.DottedChart.DottedChartException;
import org.processmining.logprojection.plugins.dottedchart.ui.DottedChartInspector;
import org.processmining.servicelevelagreements.model.eventdatabase.EventDatabase;
import org.processmining.servicelevelagreements.model.interval.Interval;
import org.processmining.servicelevelagreements.model.interval.IntervalList;

import com.google.common.collect.Table;

@Plugin(
		name = "@0 Visualize EventDatabase as Dotted Chart",
		returnLabels = { "Visualize EventDatabase" },
		returnTypes = { JComponent.class },
		parameterLabels = { "EventDatabase" },
		userAccessible = true,
		handlesCancel = false,
		quality = PluginQuality.Fair)
@Visualizer
public class EventDatabaseDottedChartVisualizer {

	private final ExecutorService executor = Executors.newCachedThreadPool();

	@PluginVariant(
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext uiPluginContext, EventDatabase eventDatabase)
			throws DottedChartException {
		long time = -System.currentTimeMillis();
		System.out.println("[EventDatabaseVisualizer] Start.");

		DottedChartInspector panel = new DottedChartInspector(
				new LogView(convertEventDatabaseToXLog(eventDatabase), uiPluginContext.getProgress()), uiPluginContext);

		time += System.currentTimeMillis();
		System.out.println("[EventDatabaseVisualizer] End (took " + DurationFormatUtils.formatDurationHMS(time) + ").");

		return panel;

	}

	private XLog convertEventDatabaseToXLog(EventDatabase eventDatabase) {

		XFactory xFactory = XFactoryRegistry.instance().currentDefault();

		XLog xlog = xFactory.createLog();

		XAttribute conceptNameXAttribute = xFactory.createAttributeLiteral(XConceptExtension.KEY_NAME, "UNKNOWN",
				XConceptExtension.instance());
		XAttribute lifeCycleXAttribute = xFactory.createAttributeLiteral(XLifecycleExtension.KEY_MODEL,
				XLifecycleExtension.VALUE_MODEL_STANDARD, XLifecycleExtension.instance());
		XAttribute timestampXAttribute = xFactory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP, 0l,
				XTimeExtension.instance());

		xlog.getGlobalTraceAttributes().add(conceptNameXAttribute);
		//		xlog.getGlobalTraceAttributes().add(timestampXAttribute);
		xlog.getGlobalEventAttributes().add(conceptNameXAttribute);
		xlog.getGlobalEventAttributes().add(lifeCycleXAttribute);
		xlog.getGlobalEventAttributes().add(timestampXAttribute);

		Map<String, Table<String, String, IntervalList>> fluentArgumentValueMVIs = eventDatabase.getFluentValueMVIs();

		long lastTimeStamp = eventDatabase.getLastTimeStamp();

		for (String fluent : fluentArgumentValueMVIs.keySet()) {

			Table<String, String, IntervalList> argumentValueMVIs = fluentArgumentValueMVIs.get(fluent);

			for (String arguments : argumentValueMVIs.rowKeySet()) {

				XTrace xTrace = xFactory.createTrace();
				XConceptExtension.instance().assignName(xTrace, arguments);

				for (String value : argumentValueMVIs.row(arguments).keySet()) {

					IntervalList MVI = argumentValueMVIs.get(arguments, value);

					for (Interval I : MVI) {

						if (!I.isEmpty()) {
							XEvent startXEvent = xFactory.createEvent();
							XConceptExtension.instance().assignName(startXEvent, fluent + " " + value);
							XLifecycleExtension.instance().assignStandardTransition(startXEvent, StandardModel.START);
							XLifecycleExtension.instance().assignTransition(startXEvent,
									StandardModel.START.getEncoding());
							XTimeExtension.instance().assignTimestamp(startXEvent, I.getStartTimestamp());
							xTrace.add(startXEvent);

							XEvent endXEvent = xFactory.createEvent();
							XConceptExtension.instance().assignName(endXEvent, fluent + " " + value);
							XLifecycleExtension.instance().assignStandardTransition(endXEvent, StandardModel.COMPLETE);
							XLifecycleExtension.instance().assignTransition(endXEvent,
									StandardModel.COMPLETE.getEncoding());
							if (!I.isOpenEnded()) {
								XTimeExtension.instance().assignTimestamp(endXEvent, I.getEndTimestamp() - 1);
								xTrace.add(endXEvent);
							} else {
								XTimeExtension.instance().assignTimestamp(endXEvent, lastTimeStamp);
							}
						}

					}

				}

				xlog.add(xTrace);

			}

		}

		return xlog;
	}

}
