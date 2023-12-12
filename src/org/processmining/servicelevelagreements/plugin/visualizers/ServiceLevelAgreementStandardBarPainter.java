package org.processmining.servicelevelagreements.plugin.visualizers;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.RectangularShape;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.ui.RectangleEdge;

public class ServiceLevelAgreementStandardBarPainter extends StandardBarPainter {

	private static final long serialVersionUID = -5225633872625038138L;

	/**
	 * Paints a single bar instance.
	 *
	 * @param g2
	 *            the graphics target.
	 * @param renderer
	 *            the renderer.
	 * @param row
	 *            the row index.
	 * @param column
	 *            the column index.
	 * @param bar
	 *            the bar
	 * @param base
	 *            indicates which side of the rectangle is the base of the bar.
	 */
	@Override
	public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar,
			RectangleEdge base) {

		g2.fill(bar);

		// draw the outline...
		if (renderer.isDrawBarOutline()) {
			// && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
			Stroke stroke = renderer.getItemOutlineStroke(row, column);
			Paint paint = renderer.getItemOutlinePaint(row, column);
			if (stroke != null && paint != null) {
				g2.setStroke(stroke);
				g2.setPaint(paint);
				g2.draw(bar);
			}
		}

	}

}
