package org.processmining.servicelevelagreements.plugin.visualizers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeriesCollection;

/**
 * Based on code from
 * {@link http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=25380}
 * 
 * @author B.F.A. Hompes <b.f.a.hompes@tue.nl>
 *
 */
public class TaskSubTaskIntervalCategoryToolTipGenerator extends IntervalCategoryToolTipGenerator {

	private static final long serialVersionUID = 119180626013035218L;

	private int subTaskIndex;

	public TaskSubTaskIntervalCategoryToolTipGenerator() {
		super();
		subTaskIndex = 0;
	}

	public TaskSubTaskIntervalCategoryToolTipGenerator(String labelFormat, NumberFormat format) {
		super(labelFormat, format);
		subTaskIndex = 0;
	}

	public TaskSubTaskIntervalCategoryToolTipGenerator(String labelFormat, DateFormat format) {
		super(labelFormat, format);
		subTaskIndex = 0;
	}

	/**
	 * Creates the array of items that can be passed to the
	 * <code>MessageFormat</code> class for creating labels.
	 *
	 * @param dataset
	 *            the dataset (<code>null</code> not permitted).
	 * @param row
	 *            the row index (zero-based).
	 * @param column
	 *            the column index (zero-based).
	 *
	 * @return The items (never <code>null</code>).
	 */
	@Override
	protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
		Object[] result = new Object[6];
		result[0] = dataset.getRowKey(row).toString();
		result[1] = dataset.getColumnKey(column).toString();
		Number value = dataset.getValue(row, column);
		if (getNumberFormat() != null) {
			result[2] = getNumberFormat().format(value);
		} else if (getDateFormat() != null) {
			result[2] = getDateFormat().format(value);
		}

		if (dataset instanceof TaskSeriesCollection) {
			TaskSeriesCollection data = (TaskSeriesCollection) dataset;
			if (data.getSeries(row).get(column).getSubtaskCount() > 0) {
				Task subTask = data.getSeries(row).get(column).getSubtask(subTaskIndex);
				Date start = subTask.getDuration().getStart();
				Date end = subTask.getDuration().getEnd();
				if (getNumberFormat() != null) {
					result[3] = getNumberFormat().format(0.0);
					result[4] = getNumberFormat().format(0.0);
					result[5] = "";
				} else if (getDateFormat() != null) {
					result[3] = getDateFormat().format(start);
					result[4] = getDateFormat().format(end);
					result[5] = data.getSeries(row).get(column).getSubtask(subTaskIndex).getDescription();
				}
			} else {
				Task task = data.getSeries(row).get(column);
				Date start = task.getDuration().getStart();
				Date end = task.getDuration().getEnd();
				if (getNumberFormat() != null) {
					result[3] = getNumberFormat().format(0.0);
					result[4] = getNumberFormat().format(0.0);
					result[5] = "";
				} else if (getDateFormat() != null) {
					result[3] = getDateFormat().format(start);
					result[4] = getDateFormat().format(end);
					result[5] = "";
				}
			}
		} else if (dataset instanceof IntervalCategoryDataset) {
			IntervalCategoryDataset icd = (IntervalCategoryDataset) dataset;
			Number start = icd.getStartValue(row, column);
			Number end = icd.getEndValue(row, column);
			if (getNumberFormat() != null) {
				result[3] = getNumberFormat().format(start);
				result[4] = getNumberFormat().format(end);
				result[5] = "";
			} else if (getDateFormat() != null) {
				result[3] = getDateFormat().format(start);
				result[4] = getDateFormat().format(end);
				result[5] = "";
			}
		}

		return result;
	}

	public void setSubTaskIndex(int subTaskIndex) {
		this.subTaskIndex = subTaskIndex;
	}
}
