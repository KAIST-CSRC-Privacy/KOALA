package kr.ac.kaist.csrc.koala.gui.structure;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataSubset;
import javax.swing.table.DefaultTableModel;
import java.text.ParseException;

public class QueryTableBuilder {
	Data data;
	DataSelector dataSelector;
	DataSubset dataSubset;
	DefaultTableModel queryTable;

	public QueryTableBuilder(Data data, String query) {
		this.data = data;
		try {
			dataSelector = DataSelector.create(data, query);
			dataSubset = DataSubset.create(data, dataSelector);
			subsetTableBuilder();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void subsetTableBuilder() {
		//query result RowSet
		int columnNum = data.getHandle().getNumColumns();
		int[] selectedIndices = dataSubset.getArray();
		String[] columnNames = new String[columnNum];
		for (int i = 0; i < columnNum; i++) {
			columnNames[i] = data.getHandle().getAttributeName(i);
		}
		queryTable = new DefaultTableModel(columnNames, 0);

		for (int i : selectedIndices) {
			Object[] row = new Object[columnNum];
			for (int j = 0; j < columnNum; j++) {
				row[j] = data.getHandle().getValue(i, j);
			}
			queryTable.addRow(row);
		}
	}

	public DefaultTableModel getQueryTable() {
		return queryTable;
	}

	public DataSubset getDataSubset() {
		return dataSubset;
	}
}
