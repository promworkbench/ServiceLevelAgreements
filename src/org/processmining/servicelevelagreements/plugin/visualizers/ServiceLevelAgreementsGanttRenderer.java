package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.processmining.logprojection.plugins.dottedchart.ColorUtils;

public class ServiceLevelAgreementsGanttRenderer extends GanttRenderer {

	private static final long serialVersionUID = -7139232508204373256L;

	static Logger logger = Logger.getLogger(ServiceLevelAgreementsGanttRenderer.class);

	Map<String, Color> valueToColorMap;
	int defaultColors = 3;

	/**
	 * Creates a new renderer.
	 */
	public ServiceLevelAgreementsGanttRenderer() {
		super();
		setCompletePaint(Color.DARK_GRAY);
		setIncompletePaint(Color.LIGHT_GRAY);
		setDrawBarOutline(true);
		setBarPainter(new ServiceLevelAgreementStandardBarPainter());

		valueToColorMap = new HashMap<String, Color>();
		// Update defaultColors if this is changed!
		valueToColorMap.put("pending", Color.CYAN);
		valueToColorMap.put("satisfied", Color.LIGHT_GRAY);
		valueToColorMap.put("violated", Color.DARK_GRAY);
	}

	/**
	 * Draws the tasks/subtasks for one item.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the data plot area.
	 * @param plot
	 *            the plot.
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the data.
	 * @param row
	 *            the row index (zero-based).
	 * @param column
	 *            the column index (zero-based).
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected void drawTasks(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot,
			CategoryAxis domainAxis, ValueAxis rangeAxis, GanttCategoryDataset dataset, int row, int column) {

		TaskSeriesCollection data = null;

		// Create a color map
		if (dataset instanceof TaskSeriesCollection) {
			data = (TaskSeriesCollection) dataset;
			// Find the base task description, give it a color.
			//			String slaValue = data.getSeries(row).get(column).getDescription();
			//			if (!valueToColorMap.containsKey(slaValue))
			//				valueToColorMap.put(slaValue, null);
			// Find all possible subtask descriptions, those are SLA values.
			for (int i = 0; i < data.getSeries(row).get(column).getSubtaskCount(); i++) {
				String slaValue = data.getSeries(row).get(column).getSubtask(0).getDescription();
				if (!valueToColorMap.containsKey(slaValue))
					valueToColorMap.put(slaValue, null);
			}
			// Give each value its own color.
			Color[] colors = ColorUtils.generateVisuallyDistinctColors(valueToColorMap.keySet().size() - defaultColors,
					1, 0);
			List<String> valueList = new ArrayList<String>();
			valueList.addAll(valueToColorMap.keySet());
			for (int i = defaultColors; i < valueList.size(); i++) {
				valueToColorMap.put(valueList.get(i), colors[i - defaultColors]);
			}
		}

		int count = dataset.getSubIntervalCount(row, column);
		if (count == 0) {
			drawTask(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column);
		}

		PlotOrientation orientation = plot.getOrientation();
		for (int subinterval = 0; subinterval < count; subinterval++) {

			RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

			// value 0
			Number value0 = dataset.getStartValue(row, column, subinterval);
			if (value0 == null) {
				return;
			}
			double translatedValue0 = rangeAxis.valueToJava2D(value0.doubleValue(), dataArea, rangeAxisLocation);

			// value 1
			Number value1 = dataset.getEndValue(row, column, subinterval);
			if (value1 == null) {
				return;
			}
			double translatedValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation);

			if (translatedValue1 < translatedValue0) {
				double temp = translatedValue1;
				translatedValue1 = translatedValue0;
				translatedValue0 = temp;
			}

			double rectStart = calculateBarW0(plot, plot.getOrientation(), dataArea, domainAxis, state, row, column);
			double rectLength = Math.abs(translatedValue1 - translatedValue0);
			double rectBreadth = state.getBarWidth();

			// DRAW THE BARS...
			Rectangle2D bar = null;
			RectangleEdge barBase = null;
			if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
				bar = new Rectangle2D.Double(translatedValue0, rectStart, rectLength, rectBreadth);
				barBase = RectangleEdge.LEFT;
			} else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				bar = new Rectangle2D.Double(rectStart, translatedValue0, rectBreadth, rectLength);
				barBase = RectangleEdge.BOTTOM;
			}

			Rectangle2D completeBar = null;
			Rectangle2D incompleteBar = null;
			Number percent = dataset.getPercentComplete(row, column, subinterval);
			double start = getStartPercent();
			double end = getEndPercent();
			if (percent != null) {
				double p = percent.doubleValue();
				if (orientation == PlotOrientation.HORIZONTAL) {
					completeBar = new Rectangle2D.Double(translatedValue0, rectStart + start * rectBreadth,
							rectLength * p, rectBreadth * (end - start));
					incompleteBar = new Rectangle2D.Double(translatedValue0 + rectLength * p,
							rectStart + start * rectBreadth, rectLength * (1 - p), rectBreadth * (end - start));
				} else if (orientation == PlotOrientation.VERTICAL) {
					completeBar = new Rectangle2D.Double(rectStart + start * rectBreadth,
							translatedValue0 + rectLength * (1 - p), rectBreadth * (end - start), rectLength * p);
					incompleteBar = new Rectangle2D.Double(rectStart + start * rectBreadth, translatedValue0,
							rectBreadth * (end - start), rectLength * (1 - p));
				}
			}

			if (data != null) {
				g2.setPaint(
						valueToColorMap.get(data.getSeries(row).get(column).getSubtask(subinterval).getDescription()));
			}
			getBarPainter().paintBar(g2, this, row, column, bar, barBase);

			if (completeBar != null) {
				g2.setPaint(getCompletePaint());
				g2.fill(completeBar);
			}
			if (incompleteBar != null) {
				g2.setPaint(getIncompletePaint());
				g2.fill(incompleteBar);
			}
			if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
				g2.setStroke(getItemStroke(row, column));
				g2.setPaint(getItemOutlinePaint(row, column));
				g2.draw(bar);
			}

			if (subinterval == count - 1) {
				// submit the current data point as a crosshair candidate
				int datasetIndex = plot.indexOf(dataset);
				Comparable columnKey = dataset.getColumnKey(column);
				Comparable rowKey = dataset.getRowKey(row);
				double xx = domainAxis.getCategorySeriesMiddle(columnKey, rowKey, dataset, getItemMargin(), dataArea,
						plot.getDomainAxisEdge());
				updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column),
						value1.doubleValue(), datasetIndex, xx, translatedValue1, orientation);
			}
			// collect entity and tool tip information...
			if (state.getInfo() != null) {
				EntityCollection entities = state.getEntityCollection();
				if (entities != null) {
					addItemEntity(entities, dataset, row, column, bar, subinterval);
				}
			}
		}
	}

	/**
	 * Draws a single task.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the data plot area.
	 * @param plot
	 *            the plot.
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the data.
	 * @param row
	 *            the row index (zero-based).
	 * @param column
	 *            the column index (zero-based).
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected void drawTask(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot,
			CategoryAxis domainAxis, ValueAxis rangeAxis, GanttCategoryDataset dataset, int row, int column) {

		TaskSeriesCollection data = null;
		if (dataset instanceof TaskSeriesCollection) {
			data = (TaskSeriesCollection) dataset;
		}

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

		// Y0
		Number value0 = dataset.getEndValue(row, column);
		if (value0 == null) {
			return;
		}
		double java2dValue0 = rangeAxis.valueToJava2D(value0.doubleValue(), dataArea, rangeAxisLocation);

		// Y1
		Number value1 = dataset.getStartValue(row, column);
		if (value1 == null) {
			return;
		}
		double java2dValue1 = rangeAxis.valueToJava2D(value1.doubleValue(), dataArea, rangeAxisLocation);

		if (java2dValue1 < java2dValue0) {
			double temp = java2dValue1;
			java2dValue1 = java2dValue0;
			java2dValue0 = temp;
			value1 = value0;
		}

		double rectStart = calculateBarW0(plot, orientation, dataArea, domainAxis, state, row, column);
		double rectBreadth = state.getBarWidth();
		double rectLength = Math.abs(java2dValue1 - java2dValue0);

		Rectangle2D bar = null;
		RectangleEdge barBase = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(java2dValue0, rectStart, rectLength, rectBreadth);
			barBase = RectangleEdge.LEFT;
		} else if (orientation == PlotOrientation.VERTICAL) {
			bar = new Rectangle2D.Double(rectStart, java2dValue1, rectBreadth, rectLength);
			barBase = RectangleEdge.BOTTOM;
		}

		Rectangle2D completeBar = null;
		Rectangle2D incompleteBar = null;
		Number percent = dataset.getPercentComplete(row, column);
		double start = getStartPercent();
		double end = getEndPercent();
		if (percent != null) {
			double p = percent.doubleValue();
			if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
				completeBar = new Rectangle2D.Double(java2dValue0, rectStart + start * rectBreadth, rectLength * p,
						rectBreadth * (end - start));
				incompleteBar = new Rectangle2D.Double(java2dValue0 + rectLength * p, rectStart + start * rectBreadth,
						rectLength * (1 - p), rectBreadth * (end - start));
			} else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				completeBar = new Rectangle2D.Double(rectStart + start * rectBreadth,
						java2dValue1 + rectLength * (1 - p), rectBreadth * (end - start), rectLength * p);
				incompleteBar = new Rectangle2D.Double(rectStart + start * rectBreadth, java2dValue1,
						rectBreadth * (end - start), rectLength * (1 - p));
			}
		}

		if (data != null) {
			g2.setPaint(valueToColorMap.get(data.getSeries(row).get(column).getDescription()));
		}
		getBarPainter().paintBar(g2, this, row, column, bar, barBase);

		if (completeBar != null) {
			g2.setPaint(getCompletePaint());
			g2.fill(completeBar);
		}
		if (incompleteBar != null) {
			g2.setPaint(getIncompletePaint());
			g2.fill(incompleteBar);
		}

		// draw the outline...
		if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
			Stroke stroke = getItemOutlineStroke(row, column);
			Paint paint = getItemOutlinePaint(row, column);
			if (stroke != null && paint != null) {
				g2.setStroke(stroke);
				g2.setPaint(paint);
				g2.draw(bar);
			}
		}

		CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
		if (generator != null && isItemLabelVisible(row, column)) {
			drawItemLabel(g2, dataset, row, column, plot, generator, bar, false);
		}

		// submit the current data point as a crosshair candidate
		int datasetIndex = plot.indexOf(dataset);
		Comparable columnKey = dataset.getColumnKey(column);
		Comparable rowKey = dataset.getRowKey(row);
		double xx = domainAxis.getCategorySeriesMiddle(columnKey, rowKey, dataset, getItemMargin(), dataArea,
				plot.getDomainAxisEdge());
		updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column),
				value1.doubleValue(), datasetIndex, xx, java2dValue1, orientation);

		// collect entity and tool tip information...
		EntityCollection entities = state.getEntityCollection();
		if (entities != null) {
			addItemEntity(entities, dataset, row, column, bar);
		}
	}

	/**
	 * 
	 * Adds an entity with the specified hotspot.
	 *
	 * @param entities
	 *            the entity collection.
	 * @param dataset
	 *            the dataset.
	 * @param row
	 *            the row index.
	 * @param column
	 *            the column index.
	 * @param hotspot
	 *            the hotspot (<code>null</code> not permitted).
	 * @param subTaskIndex
	 *            the index of the subtask
	 */
	protected void addItemEntity(EntityCollection entities, CategoryDataset dataset, int row, int column, Shape hotspot,
			int subTaskIndex) {
		ParamChecks.nullNotPermitted(hotspot, "hotspot");
		if (!getItemCreateEntity(row, column)) {
			return;
		}
		String tip = null;
		CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
		if (tipster != null) {
			((TaskSubTaskIntervalCategoryToolTipGenerator) tipster).setSubTaskIndex(subTaskIndex);
			tip = tipster.generateToolTip(dataset, row, column);
		}
		String url = null;
		CategoryURLGenerator urlster = getItemURLGenerator(row, column);
		if (urlster != null) {
			url = urlster.generateURL(dataset, row, column);
		}
		CategoryItemEntity entity = new CategoryItemEntity(hotspot, tip, url, dataset, dataset.getRowKey(row),
				dataset.getColumnKey(column));
		entities.add(entity);
	}

	private Paint getCategoryPaint(String description) {
		Paint result = Color.black;
		switch (description) {
			case "pending" :
				result = Color.BLUE;
				break;
			case "satisfied" :
				result = Color.GREEN;
				break;
			case "violated" :
				result = Color.RED;
				break;
		}
		logger.debug(description);
		return result;
	}
}
