package kr.ac.kaist.csrc.koala.gui.tableRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SortTableCellRenderer extends DefaultTableCellRenderer {

	private final Color EVEN_COLOR = new Color(240, 240, 240);
	private final Color ODD_COLOR = new Color(255, 255, 255);
	private final Map<Integer, Color> rowColorCache = new HashMap<>();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
												   int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// Check if the color cache needs to be recomputed (e.g., if the table's row count changes)
		if (rowColorCache.isEmpty() || rowColorCache.size() != table.getRowCount()) {
			computeRowColors(table, column); // Assuming 'lastQid' or similar logic is handled elsewhere
		}
		c.setBackground(rowColorCache.getOrDefault(row, EVEN_COLOR)); // Set the background color from cache
		return c;
	}

	/**
	 * Computes and caches the background color for each row based on the value in a specific column.
	 * Rows with different values than the previous row alternate between even and odd colors.
	 *
	 * @param table The JTable for which to compute row colors.
	 * @param columnIndex The index of the column based on whose values the coloring is applied.
	 */
	private void computeRowColors(JTable table, int columnIndex) {
		rowColorCache.clear();

		String previousValue = null;
		Color currentColor = EVEN_COLOR;

		for (int i = 0; i < table.getRowCount(); i++) {
			Object cellValue = table.getValueAt(i, columnIndex);
			String currentValue = cellValue != null ? cellValue.toString() : "";
			if (!currentValue.equals(previousValue)) {
				// Toggle color for a new group of values
				currentColor = (currentColor == EVEN_COLOR) ? ODD_COLOR : EVEN_COLOR;
			}
			rowColorCache.put(i, currentColor);
			previousValue = currentValue;
		}
	}
}
