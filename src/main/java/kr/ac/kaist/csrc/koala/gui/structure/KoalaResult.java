package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.gui.tableRenderer.SortRiskCellRenderer;
import kr.ac.kaist.csrc.koala.gui.tableRenderer.SortTableCellRenderer;
import org.deidentifier.arx.ARXResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.stream.Stream;


public class KoalaResult {

	private final KoalaInput koalaInput;
	private final DefaultTableModel originalTable;
	private final DefaultTableModel anonymizedTable;
	private final DefaultTableModel anonymizedSummaryTable;
	private final DefaultTableModel riskTable;
	private final DefaultTableModel riskTableSelected;
	public String sensitiveAttribute;
	private final String[] attributeOrdered;
	private final String[] equivalenceClassStringArray;
    public JPanel summaryPanel;
	private final ARXResult arxResult;

	public KoalaResult(KoalaInput koalaInput, ARXResult arxResult) {
		this.arxResult = arxResult;
		this.koalaInput = koalaInput;
		this.originalTable = generalTableClone(koalaInput.getLoadedTable());
		anonymizedTable = resultToTable(arxResult);
		sensitiveAttribute = getSensitiveAttribute(arxResult);
		attributeOrdered = getOrderedAttributes(arxResult);
		equivalenceClassStringArray = getEquivalenceClassStringArray(attributeOrdered);
		anonymizedSummaryTable = anonymizedSummaryBuilder();
        double threshold = 0.1;
		summaryPanel = new JPanel();
		riskTable = riskTableBuilder();
		riskTableSelected = riskTableSelectedBuilder(riskTable, threshold);
	}

	private DefaultTableModel resultToTable(ARXResult arxResult) {
		Iterator<String[]> output = arxResult.getOutput(false).iterator();
		String[] columnName = output.next();
		DefaultTableModel outputTable = new DefaultTableModel(columnName, 0);
		while (output.hasNext()) {
			String[] rowData = output.next();
			outputTable.addRow(rowData);
		}

		return outputTable;
	}

	private String[] getColumnNames() {
		String[] output = new String[anonymizedTable.getColumnCount()];
		for (int i = 0; i < anonymizedTable.getColumnCount(); i++) {
			output[i] = anonymizedTable.getColumnName(i);
		}
		return output;
	}

	private String[] getColumnNames(DefaultTableModel inputTable) {
		int columnCounts = inputTable.getColumnCount();
		String[] output = new String[columnCounts];
		for (int i = 0; i < columnCounts; i++) {
			output[i] = inputTable.getColumnName(i);
		}
		return output;
	}

	private int getColumnIndexOf(String target) {
		String[] temp = getColumnNames();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].equals(target)) {
				return i;
			}
		}
		return 0;
	}

	private int getColumnIndexOf(String target, DefaultTableModel inputTable) {
		String[] temp = getColumnNames(inputTable);
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].equals(target)) {
				return i;
			}
		}
		return 0;
	}

	private int[] getColumnIndexOf(String[] targets, DefaultTableModel inputTable) {
		String[] temp = getColumnNames(inputTable);
		ArrayList<Integer> output = new ArrayList<>();
		for (String j : targets) {
			for (int i = 0; i < temp.length; i++) {
				if (j.equals(temp[i])) {
					output.add(i);
				}
			}
		}
		return output.stream().mapToInt(i -> i).toArray();
	}

	private String[] getColumnStringArray(int index) {

		String[] output = new String[anonymizedTable.getRowCount()];
		for (int i = 0; i < anonymizedTable.getRowCount(); i++) {
			output[i] = (String) anonymizedTable.getValueAt(i, index);
		}
		return output;
	}

	public String[] getColumnStringArray(String target) {

		String[] output = new String[anonymizedTable.getRowCount()];
		int targetIndex = getColumnIndexOf(target);
		for (int i = 0; i < anonymizedTable.getRowCount(); i++) {
			output[i] = (String) anonymizedTable.getValueAt(i, targetIndex);
		}
		return output;
	}

	protected String[] getColumnStringArray(DefaultTableModel inputTable, String target) {

		String[] output = new String[inputTable.getRowCount()];
		int targetIndex = getColumnIndexOf(target, inputTable);
		for (int i = 0; i < inputTable.getRowCount(); i++) {
			output[i] = String.valueOf(inputTable.getValueAt(i, targetIndex));
		}
		return output;
	}

	private String[] getAnonymizedRowStringArray(int index) {
		String[] output = new String[anonymizedTable.getColumnCount()];
		for (int i = 0; i < anonymizedTable.getColumnCount(); i++) {
			output[i] = (String) anonymizedTable.getValueAt(index, i);
		}
		return output;
	}

	private String[] getOriginalRowStringArray(int index) {
		String[] output = new String[originalTable.getColumnCount()];
		for (int i = 0; i < originalTable.getColumnCount(); i++) {
			output[i] = (String) originalTable.getValueAt(index, i);
		}
		return output;
	}

	private String[] getGeneralRowStringArray(DefaultTableModel table, int index) {
		String[] output = new String[table.getColumnCount()];
		for (int i = 0; i < table.getColumnCount(); i++) {
			output[i] = (String) table.getValueAt(index, i);
		}
		return output;
	}

	private String[] getRowStringArrayExceptColumn(DefaultTableModel table, int index, int columnIndex) {
		String[] output = new String[table.getColumnCount()];
		for (int i = 0; i < table.getColumnCount(); i++) {
			if (i == columnIndex) {
				continue;
			}
			output[i] = (String) table.getValueAt(index, i);
		}
		return output;
	}

	private DefaultTableModel sortByColumn(String target, DefaultTableModel inputTable, boolean removal,
		boolean isAnonymized) {

		String[] targetString = getColumnStringArray(inputTable, target);
		Integer[] indices = new Integer[targetString.length];
		int[] orderedIndices = new int[targetString.length];
		for (int i = 0; i < targetString.length; i++) {
			indices[i] = i;
		}
		Arrays.sort(indices, Comparator.comparing(i -> targetString[i]));
		for (int i = 0; i < indices.length; i++) {
			orderedIndices[i] = indices[i];
		}
		DefaultTableModel outputTable = new DefaultTableModel(getColumnNames(inputTable), 0);

		if (isAnonymized) {
			for (int i : orderedIndices) {
				outputTable.addRow(getAnonymizedRowStringArray(i));
			}
		} else {
			for (int i : orderedIndices) {
				outputTable.addRow(getOriginalRowStringArray(i));
			}
		}
		if (removal) {
			return removeColumn(outputTable, target);
		}

		return outputTable;
	}

	private ArrayList<Integer> rankArrayList(ArrayList<Integer> nums) {
		int[] temp = new int[nums.size()];
		ArrayList<Integer> outputArrayList = new ArrayList<>();
		for (int i = 0; i < nums.size(); i++) {
			temp[i] = nums.get(i);
		}
		int[] sorted = temp.clone();
		Arrays.sort(sorted);

        for (int num : temp) {
            int index = 0;
            for (int k = 0; k < sorted.length; k++) {
                if (sorted[k] == num) {
                    index = k;
                    sorted[k] = Integer.MIN_VALUE;
                    break;
                }
            }
            outputArrayList.add(index);
        }
		return outputArrayList;
	}

	private int distinctNumber(String[] column) {
		ArrayList<String> temp = new ArrayList<>();
		for (String i : column) {
			if (temp.contains(i)) {
				continue;
			}
			temp.add(i);
		}
		return temp.size();
	}

	protected String[] distinctColumnValue(String[] column) {
		ArrayList<String> temp = new ArrayList<>();
		String[] output;
		for (String i : column) {
			if (temp.contains(i)) {
				continue;
			}
			temp.add(i);
		}
		output = temp.toArray(new String[temp.size()]);
		return output;
	}

	private DefaultTableModel removeColumn(DefaultTableModel table, String attribute) {
		int targetIndex = getColumnIndexOf(attribute, table);
		ArrayList<String> removedColumnNames = new ArrayList<>();
		for (String i : getColumnNames(table)) {
			if (i.equals(attribute)) {
				continue;
			} else {
				removedColumnNames.add(i);
			}
		}
		DefaultTableModel outputTable = new DefaultTableModel(removedColumnNames.toArray(), 0);
		for (int i = 0; i < table.getRowCount(); i++) {
			outputTable.addRow(getRowStringArrayExceptColumn(table, i, targetIndex));
		}
		return (outputTable);
	}

	public String getLastQidAttribute() {
		if (koalaInput.sensitiveType.equals("   ")) {
			return attributeOrdered[attributeOrdered.length - 1];
		} else {
			return attributeOrdered[attributeOrdered.length - 2];
		}
	}

	private String[] getOrderedAttributes(ARXResult arxResult) {
		ArrayList<String> qidValues = new ArrayList<>();
		ArrayList<Integer> distinctNumbers = new ArrayList<>();
		for (String i : arxResult.getDataDefinition().getQuasiIdentifyingAttributes()) {
			qidValues.add(i);
			distinctNumbers.add(distinctNumber(getColumnStringArray(anonymizedTable, i)));
		}
        String[] output;
        ArrayList<Integer> ranks = rankArrayList(distinctNumbers);
        if (koalaInput.sensitiveType.equals("   ")) {
            output = new String[qidValues.size()];
            for (int i = 0; i < qidValues.size(); i++) {
				output[ranks.get(i)] = qidValues.get(i);
			}
        } else {
            output = new String[qidValues.size() + 1];
            for (int i = 0; i < qidValues.size(); i++) {
				output[ranks.get(i)] = qidValues.get(i);
			}
			output[qidValues.size()] = koalaInput.sensitiveValue;
        }
        return output;
    }

	private String getSensitiveAttribute(ARXResult arxResult) {
		if (!arxResult.getDataDefinition().getSensitiveAttributes().isEmpty()) {
			for (String i : arxResult.getDataDefinition().getSensitiveAttributes()) {
				return i;
			}
		}
		return null;
	}

	private String[] getEquivalenceClassStringArray(String[] attributes) {
		ArrayList<String[]> temp = new ArrayList<>();
		String[] output = new String[originalTable.getRowCount()];
		for (String i : attributes) {
			temp.add(getColumnStringArray(i));
		}
		for (int i = 0; i < originalTable.getRowCount(); i++) {
			StringBuilder tempString = new StringBuilder();
			for (String[] strings : temp) {
				tempString.append(strings[i]);
			}
			output[i] = tempString.toString();
		}
		return output;
	}

	public DefaultTableModel anonymizedSummaryBuilder() {
		DefaultTableModel temp = getSelectedTable(getAnonymizedTableSorted(), attributeOrdered);
		DefaultTableModel outputTable = new DefaultTableModel(attributeOrdered, 0);
		ArrayList<Integer> counts = new ArrayList<>();
		ArrayList<Integer> equivalenceClassIndex = new ArrayList<>();
		String[] lastQidColumn = getColumnStringArray(temp, getLastQidAttribute());
		String[] criticalColumn;

		if (koalaInput.sensitiveType.equals("    ")) {
			criticalColumn = getColumnStringArray(temp, getLastQidAttribute());
		} else {
			criticalColumn = getColumnStringArray(temp, koalaInput.sensitiveValue);
		}

		int count = 1;
		int ec = 1;

		for (int i = 0; i < temp.getRowCount(); i++) {
			// First Row
			if (i == 0) {
				outputTable.addRow(getRow(temp, i));
				equivalenceClassIndex.add(ec);
				continue;
			}

			// General
			if (criticalColumn[i].equals(criticalColumn[i - 1])) {
				count++;
			} else {
				outputTable.addRow(getRow(temp, i));
				counts.add(count);
				count = 1;
				if (!lastQidColumn[i].equals(lastQidColumn[i - 1])) {
					ec++;
				}
				equivalenceClassIndex.add(ec);
			}

			// Last Row
			if (i == temp.getRowCount() - 1) {
				counts.add(count);
			}
		}

		outputTable.addColumn("counts", counts.toArray());
		outputTable.addColumn("EC", equivalenceClassIndex.toArray());

		return outputTable;
	}

	public DefaultTableModel riskTableBuilder() {
		ArrayList<String> riskRate = new ArrayList<>();
		ArrayList<String> ecIndices = new ArrayList<>();

		DefaultTableModel output = generalTableClone(getAnonymizedTableSorted());
		int[] counts = Stream.of(getColumnStringArray(getAnonymizedSummary(), "counts"))
			.mapToInt(Integer::parseInt)
			.toArray();
		String[] EC = getColumnStringArray(getAnonymizedSummary(), "EC");

		int[] frequency = countOccurrences(EC);
		int ECIndex = 0;
		int currentCountIndex = 0; // counts 배열에 대한 현재 인덱스

		for (int i : frequency) {
			ECIndex += 1;
			int totalCount = 0;
			for (int j = 0; j < i; j++) {
				if (currentCountIndex + j < counts.length) {
					totalCount += counts[currentCountIndex + j];
				} else {
					break;
				}
			}
			for (int j = 0; j < totalCount; j++) {
				ecIndices.add(String.valueOf(ECIndex));
				riskRate.add(String.format("%.5f", (double) (1.0 / totalCount)));
			}
			currentCountIndex += i;
		}
		output.addColumn("risk", riskRate.toArray());
		output.addColumn("EC", ecIndices.toArray());
		return output;
	}

	public DefaultTableModel riskTableSelectedBuilder(DefaultTableModel riskTable, double threshold) {
		DefaultTableModel resultTable = new DefaultTableModel(getColumnNames(riskTable), 0);
		int totalRowCount = riskTable.getRowCount();
		int totalColumnCount = riskTable.getColumnCount();
		for (int k = 0; k < totalRowCount; k++) {
			if (Double.parseDouble((String) riskTable.getValueAt(k, totalColumnCount - 2)) > threshold) {
				resultTable.addRow(getRow(riskTable, k));
			}
		}
		return resultTable;
	}

	private String[] getRow(DefaultTableModel table, int index) {
		int columnCount = table.getColumnCount();
		String[] output = new String[columnCount];

		for (int i = 0; i < columnCount; i++) {
			output[i] = (String) table.getValueAt(index, i);
		}

		return output;
	}

	private String[] getSelectedRow(DefaultTableModel table, int index, String[] columns) {
		int[] columnIndices = getColumnIndexOf(columns, table);
		String[] output = new String[columnIndices.length];
		for (int i = 0; i < columnIndices.length; i++) {
			output[i] = (String) table.getValueAt(index, columnIndices[i]);
		}
		return output;
	}

	private DefaultTableModel getSelectedTable(DefaultTableModel table, String[] columns) {
		DefaultTableModel output = new DefaultTableModel(columns, 0);
		for (int i = 0; i < table.getRowCount(); i++) {
			output.addRow(getSelectedRow(table, i, columns));
		}
		return output;
	}

	public DefaultTableModel originalTableClone() {
		DefaultTableModel outputTable = new DefaultTableModel(getColumnNames(), 0);
		for (int i = 0; i < originalTable.getRowCount(); i++) {
			outputTable.addRow(getOriginalRowStringArray(i));
		}
		return (outputTable);
	}

	public DefaultTableModel generalTableClone(DefaultTableModel table) {
		DefaultTableModel outputTable = new DefaultTableModel(getColumnNames(table), 0);
		for (int i = 0; i < table.getRowCount(); i++) {
			outputTable.addRow(getGeneralRowStringArray(table, i));
		}
		return (outputTable);
	}

	public JTable getAnonymizedJTableSorted() {
		JTable temp = new JTable();
		temp.setModel(getAnonymizedTableSorted());
		rendererJTable(temp);
		return temp;
	}

	public JTable getAnonymizedSummaryJTable() {
		JTable temp = new JTable();
		temp.setModel(getAnonymizedSummary());
		rendererJTable(temp);
		return temp;
	}

	public JTable getRiskJTable() {
		JTable output = new JTable();
		output.setModel(riskTable);
		output.setDefaultRenderer(Object.class, new SortRiskCellRenderer());
		return output;
	}

	public JTable getRiskSelectedJTable() {
		JTable output = new JTable();
		output.setModel(riskTableSelected);
		output.setDefaultRenderer(Object.class, new SortRiskCellRenderer());
		return output;
	}

	public DefaultTableModel getAnonymizedTable() {
		return deepCloneDefaultTableModel(anonymizedTable);
	}

	public DefaultTableModel getAnonymizedSummary() {
		return deepCloneDefaultTableModel(anonymizedSummaryTable);
	}

	public DefaultTableModel getOriginalTable() {
		return deepCloneDefaultTableModel(originalTable);
	}

	public JTable getOriginalJTableSorted() {
		JTable temp = new JTable();
		temp.setModel(getOriginalTableSorted());
		rendererJTable(temp);
		return temp;
	}

	public DefaultTableModel getAnonymizedTableSorted() {
		DefaultTableModel outputTable = getAnonymizedTable();
		outputTable.addColumn("EC", equivalenceClassStringArray);
		return (sortByColumn("EC", outputTable, true, true));
	}

	public DefaultTableModel getOriginalTableSorted() {
		DefaultTableModel outputTable = originalTableClone();
		outputTable.addColumn("EC", equivalenceClassStringArray);
		return (sortByColumn("EC", outputTable, true, false));
	}

	// Clone functions
	private void rendererJTable(JTable original) {
		original.setDefaultRenderer(Object.class, new SortTableCellRenderer());
	}

	private DefaultTableModel deepCloneDefaultTableModel(DefaultTableModel originalModel) {
		DefaultTableModel clonedModel = new DefaultTableModel();

		// column copy
		int colCount = originalModel.getColumnCount();
		for (int i = 0; i < colCount; i++) {
			clonedModel.addColumn(originalModel.getColumnName(i));
		}

		// data copy
		int rowCount = originalModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Object[] rowData = new Object[colCount];
			for (int j = 0; j < colCount; j++) {
				rowData[j] = originalModel.getValueAt(i, j);
			}
			clonedModel.addRow(rowData);
		}
		return clonedModel;
	}

	private int[] countOccurrences(String[] arr) {
		Map<String, Integer> counts = new HashMap<>();

		for (String str : arr) {
			counts.put(str, counts.getOrDefault(str, 0) + 1);
		}

		int[] result = new int[counts.size()];
		int index = 0;
		for (Integer count : counts.values()) {
			result[index++] = count;
		}

		return result;
	}

	//future use
	public DefaultTableModel createSortedTableModel(JTable table) {
		DefaultTableModel originalModel = (DefaultTableModel) table.getModel();
		DefaultTableModel sortedModel = new DefaultTableModel();

		// Add columns
		for (int col = 0; col < originalModel.getColumnCount(); col++) {
			sortedModel.addColumn(originalModel.getColumnName(col));
		}

		// Add rows based on sorted order
		for (int row = 0; row < originalModel.getRowCount(); row++) {
			int sortedRowIndex = table.convertRowIndexToModel(row);
			Object[] rowData = new Object[originalModel.getColumnCount()];
			for (int col = 0; col < originalModel.getColumnCount(); col++) {
				rowData[col] = originalModel.getValueAt(sortedRowIndex, col);
			}
			sortedModel.addRow(rowData);
		}

		return sortedModel;
	}

	public ARXResult getArxResult() {
		return arxResult;
	}
}
