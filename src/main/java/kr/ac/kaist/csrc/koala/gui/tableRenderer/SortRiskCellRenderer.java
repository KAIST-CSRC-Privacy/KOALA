package kr.ac.kaist.csrc.koala.gui.tableRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SortRiskCellRenderer extends DefaultTableCellRenderer {

	private final Color EVEN_COLOR = new Color(240, 240, 240);
	private final Color ODD_COLOR = new Color(255, 255, 255);
	private final Map<Integer, Color> rowColorCache = new HashMap<>();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
												   int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// Recompute row colors if the cache is empty or the table's row count has changed
		if (rowColorCache.isEmpty() || rowColorCache.size() != table.getRowCount()) {
			computeRowColors(table, columnNameIndex(table, "EC"));
		}

		// Apply the computed background color
		c.setBackground(rowColorCache.getOrDefault(row, EVEN_COLOR));
		return c;
	}

	private void computeRowColors(JTable table, int columnIndex) {
		rowColorCache.clear();

		Object previousValue = null;
		Color currentColor = EVEN_COLOR;

		for (int i = 0; i < table.getRowCount(); i++) {
			Object currentValue = table.getValueAt(i, columnIndex);
			// Compare values safely, considering potential nulls and ensuring string comparison
			if (previousValue != null && !currentValue.equals(previousValue)) {
				currentColor = (currentColor == EVEN_COLOR) ? ODD_COLOR : EVEN_COLOR;
			}
			rowColorCache.put(i, currentColor);
			previousValue = currentValue;
		}
	}

	private int columnNameIndex(JTable table, String columnName) {
		// Safely get the column index from its name
		return table.getColumn(columnName).getModelIndex();
	}
}
