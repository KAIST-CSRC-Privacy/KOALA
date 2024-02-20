package kr.ac.kaist.csrc.koala.utils;

import kr.ac.kaist.csrc.koala.ExprData;
import kr.ac.kaist.csrc.koala.gui.tableRenderer.SortTableCellRenderer;
import org.deidentifier.arx.Data;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DataHandle {

	public static String lastQid;
	public static int lastQidSortIndex;
	private static Color[] rowColors;

	// Converts categorical data to numerical values to find max, min, and range
	public static double[] categoricalToNum(String[] data) {
		try {
			double max = Double.parseDouble(data[0]);
			double min = max;
			for (String str : data) {
				double val = Double.parseDouble(str);
				if (val > max) max = val;
				if (val < min) min = val;
			}
			return new double[]{max, min, (max - min)};
		} catch (NumberFormatException e) {
			return new double[0]; // Returns an empty array if conversion fails
		}
	}

	// Overloaded method to handle ArrayList input for categorical to numerical conversion
	public static double[] categoricalToNum(ArrayList<String> data) {
		try {
			double max = Double.parseDouble(data.get(0));
			double min = max;
			for (String str : data) {
				double val = Double.parseDouble(str);
				if (val > max) max = val;
				if (val < min) min = val;
			}
			return new double[]{max, min, (max - min)};
		} catch (NumberFormatException e) {
			return new double[0];
		}
	}

	// Converts ARX Data object to DefaultTableModel for display in JTable
	public static DefaultTableModel dataToJTable(Data data) {
		int columnNums = data.getHandle().getNumColumns();
		int rowNums = data.getHandle().getNumRows();
		String[] columnNames = new String[columnNums];
		for (int k = 0; k < columnNums; k++) {
			columnNames[k] = data.getHandle().getAttributeName(k);
		}
		DefaultTableModel resultJTable = new DefaultTableModel(columnNames, rowNums);
		for (int i = 0; i < columnNums; i++) {
			for (int j = 0; j < rowNums; j++) {
				resultJTable.setValueAt(data.getHandle().getValue(j, i), j, i);
			}
		}
		return resultJTable;
	}

	// Converts a 2D String array to DefaultTableModel
	public static DefaultTableModel arrayToDefaultTable(String[][] array) {
		DefaultTableModel outputTable = new DefaultTableModel(0, array[0].length);
		for (String[] rowData : array) {
			outputTable.addRow(rowData);
		}
		return outputTable;
	}

	// Converts a String array to DefaultListModel for use in JList
	public static DefaultListModel arrayToListModel(String[] array) {

		DefaultListModel outputList = new DefaultListModel();
        for (String s : array) {
            outputList.addElement(s);
        }
		return outputList;
	}

	// Creates a distinct list from an original list to avoid duplicates
	public static DefaultListModel setDistinctList(DefaultListModel originalList) {
		DefaultListModel outputDistinctList = new DefaultListModel();
		for (int i = 0; i < originalList.getSize(); i++) {
			if (!outputDistinctList.contains(originalList.get(i))) {
				outputDistinctList.addElement(originalList.get(i));
			}
		}
		return outputDistinctList;
	}

	// Converts a column from JTable into DefaultListModel
	public static DefaultListModel columnToListModel(JTable jTable, int colNum) {
		DefaultListModel outputList = new DefaultListModel();
		for (int i = 0; i < jTable.getRowCount(); i++) {
			outputList.addElement(jTable.getValueAt(i, colNum));
		}
		return outputList;
	}

	// Converts an ArrayList<String> to DefaultListModel
	public static DefaultListModel arrayToListModel(ArrayList<String> inputArrayList) {
		DefaultListModel outputList = new DefaultListModel();
		for (int i = 0; i < inputArrayList.size(); i++) {
			outputList.addElement(inputArrayList.get(i));
		}
		return outputList;
	}

	// Updates a specific column in HierarchyData JTable with values from an ArrayList
	public static void setColumnArrayList(HierarchyHelper.HierarchyData inputHierarchyData,
		ArrayList<String> inputArrayList, int ColumnNum) {
		for (int i = 0; i < inputHierarchyData.hierarchyDefaultTable.getRowCount(); i++) {
			inputHierarchyData.hierarchyDefaultTable.setValueAt(inputArrayList.get(i), i, ColumnNum);
		}
		inputHierarchyData.hierarchyJTable.setModel(inputHierarchyData.hierarchyDefaultTable);
	}

	// Converts an ArrayList<String> to DefaultListModel for unique elements
	public static DefaultListModel ArrayListToList(ArrayList<String> arrayList) {
		DefaultListModel outputListModel = new DefaultListModel();
		arrayList.forEach(outputListModel::addElement);
		return outputListModel;
	}

	// Retrieves a specific row from a DefaultTableModel as a String array
	public static String[] getRowDefaultTable(DefaultTableModel inputTable, int rowIndex) {
		String[] outputRow = new String[inputTable.getColumnCount()];
		for (int i = 0; i < inputTable.getColumnCount(); i++) {
			outputRow[i] = (String) inputTable.getValueAt(rowIndex, i);
		}
		return outputRow;
	}

	// Retrieves all column names from a DefaultTableModel as a String array
	public static String[] getColumnNameDefaultTable(DefaultTableModel inputTable) {
		String[] outputColName = new String[inputTable.getColumnCount()];
		for (int i = 0; i < inputTable.getColumnCount(); i++) {
			outputColName[i] = inputTable.getColumnName(i);
		}
		return outputColName;
	}

	// Creates a deep clone of a DefaultTableModel
	public static DefaultTableModel defaultTableModelClone(DefaultTableModel inputTable) {
		DefaultTableModel outputTable = new DefaultTableModel();
		for (int i = 0; i < inputTable.getColumnCount(); i++) {
			outputTable.addColumn(inputTable.getColumnName(i));
		}
		for (int i = 0; i < inputTable.getRowCount(); i++) {
			outputTable.addRow(getRowDefaultTable(inputTable, i));
		}
		return outputTable;
	}

	// Overloaded method to generate a list of distinct values from a String array
	public static ArrayList<String> getDistinctArray(ArrayList<String> inputArrayList) {
		ArrayList<String> outputArrayList = new ArrayList<>();
		for (String i : inputArrayList) {
			if (outputArrayList.contains(i)) {
				continue;
			} else {
				outputArrayList.add(i);
			}
		}
		return outputArrayList;
	}

	public static ArrayList<String> getDistinctArray(String[] inputArrayList) {
		ArrayList<String> outputArrayList = new ArrayList<>();
		for (String i : inputArrayList) {
			if (outputArrayList.contains(i)) {
				continue;
			} else {
				outputArrayList.add(i);
			}
		}
		return outputArrayList;
	}

	public static DefaultTableModel sortByECAnonymized(DefaultTableModel inputTable, JTable setJTable, Boolean isWithSA,
		Boolean isRemoveColumn) {

		String[] addEC = addAllEquivalenceClassString(inputTable, isWithSA);
		inputTable.addColumn("addEC", addEC);
		setJTable.setModel(inputTable);

		sortByColumn(setJTable, inputTable.getColumnCount() - 1, isRemoveColumn);

		setJTable.setDefaultRenderer(Object.class, new SortTableCellRenderer());
		rowColors = new Color[setJTable.getRowCount()];
		for (int i = 0; i < setJTable.getRowCount(); i++) {
			rowColors[i] = setJTable.getCellRenderer(i, 0).
				getTableCellRendererComponent(setJTable, null, false, false, i, 0).getBackground();
		}

		return inputTable;
	}

	// Finds the number of distinct values in a specific column of a DefaultTableModel
	public static int tableColumnDistinctNum(DefaultTableModel inputTable, int columnIndex) {
		int outputNum = 0;
		ArrayList<String> testDuplicate = new ArrayList<>();
		for (int i = 0; i < inputTable.getRowCount(); i++) {
			if (!testDuplicate.contains((String) inputTable.getValueAt(i, columnIndex))) {
				testDuplicate.add((String) inputTable.getValueAt(i, columnIndex));
				outputNum = outputNum + 1;
			}
			outputNum = outputNum + 1;
		}
		return outputNum;
	}

	// Sorts a list of quasi-identifiers based on distinct value count
	public static ArrayList<String> sortQidStringList(DefaultTableModel inputTable, ArrayList<Integer> columnIndex) {
		Map<Integer, ArrayList<String>> qidSort = new HashMap<>();
		ArrayList<String> outputArray = new ArrayList<>();
        for (Integer index : columnIndex) {
            if (!qidSort.containsKey(tableColumnDistinctNum(inputTable, index))) {
                qidSort.put(tableColumnDistinctNum(inputTable, index), new ArrayList<>());
            }
            qidSort.get(tableColumnDistinctNum(inputTable, index))
                    .add(inputTable.getColumnName(index));
        }

		int[] arraySorted = new int[qidSort.keySet().size()];
		int j = 0;
		for (int i : qidSort.keySet()) {
			arraySorted[j++] = i;
		}
		Arrays.sort(arraySorted);
		for (int i : arraySorted) {
			for (int k = 0; k < qidSort.get(i).size(); k++) {
				outputArray.add(qidSort.get(i).get(k));
			}
		}
		return outputArray;
	}

	// Adds equivalence class string to all rows of a DefaultTableModel, considering sensitive attributes
	public static String[] addAllEquivalenceClassString(DefaultTableModel inputTable, Boolean isWithSA) {

		String[] resultStringArray = new String[inputTable.getRowCount()];

		ArrayList<Integer> qidNums = new ArrayList<>();
		for (int i = 0; i < ExprData.qidArrayList.size(); i++) {
			qidNums.add(inputTable.findColumn(ExprData.qidArrayList.get(i)));
		}

		ArrayList<String> temp = sortQidStringList(inputTable, qidNums);
		lastQid = temp.get(temp.size() - 1);
		lastQidSortIndex = inputTable.findColumn(lastQid);

		for (int i = 0; i < inputTable.getRowCount(); i++) {
			for (String j : temp) {
				resultStringArray[i] = resultStringArray[i] + inputTable.getValueAt(i, inputTable.findColumn(j));
			}
		}

		if (isWithSA) {
			ArrayList<Integer> saNums = new ArrayList<>();
			for (int i = 0; i < ExprData.saListModel.getSize(); i++) {
				saNums.add(inputTable.findColumn(ExprData.saListModel.get(i)));
			}
			for (int i = 0; i < inputTable.getRowCount(); i++) {
				for (int j : saNums) {
					resultStringArray[i] = resultStringArray[i] + inputTable.getValueAt(i, j);
				}
			}
		}
		return resultStringArray;
	}

	// Sorts a JTable by a specific column and optionally removes the column after sorting
	public static DefaultTableModel sortByColumn(JTable inputJTable, int columnIndex, Boolean isRemoveColumn) {
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(inputJTable.getModel());
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		inputJTable.setRowSorter(sorter);
		sorter.sort();

		if (isRemoveColumn) {
			TableColumn removeColumn = inputJTable.getColumnModel().getColumn(columnIndex);
			inputJTable.removeColumn(removeColumn);
		}
		return ((DefaultTableModel) inputJTable.getModel());
	}

	// Sets custom headers for a hierarchy table's columns
	public static void setHierarchyTableHeader(JTable inputJTable) {
		for (int i = 0; i < inputJTable.getColumnCount(); i++) {
			inputJTable.getColumnModel().getColumn(i).setHeaderValue("Level-" + i);
		}
	}

	// Retrieves a column as a String array from a JTable or DefaultTableModel by column name or index
	public static String[] getColumnString(JTable inputJTable, String columnName) {
		String[] outputString = new String[inputJTable.getRowCount()];
		int targetColumnIndex = columnNameIndex(inputJTable, columnName);

		for (int i = 0; i < inputJTable.getRowCount(); i++) {
			outputString[i] = inputJTable.getValueAt(i, targetColumnIndex).toString();
		}
		return outputString;
	}

	// Finds the index of a column by name in a JTable or DefaultTableModel
	public static String[] getColumnString(DefaultTableModel inputJTable, String columnName) {
		ArrayList<String> allNames = new ArrayList<>();
		for (int i = 0; i < inputJTable.getColumnCount(); i++) {
			allNames.add(inputJTable.getColumnName(i));
		}
		if (allNames.contains(columnName)) {
			String[] outputString = new String[inputJTable.getRowCount()];
			int targetColumnIndex = columnNameIndex(inputJTable, columnName);

			for (int i = 0; i < inputJTable.getRowCount(); i++) {
				outputString[i] = inputJTable.getValueAt(i, targetColumnIndex).toString();
			}
			return outputString;
		}
		return (new String[]{"EMPTY"});
	}

	// Finds the index of a column in a JTable by the column's name.
	public static String[] getColumnString(JTable inputJTable, int columnIndex) {
		String[] outputString = new String[inputJTable.getRowCount()];

		for (int i = 0; i < inputJTable.getRowCount(); i++) {
			outputString[i] = inputJTable.getValueAt(i, columnIndex).toString();
		}
		return outputString;
	}

	public static int columnNameIndex(JTable inputJTable, String columnName) {
		int outputInteger = -1;
		for (int i = 0; i < inputJTable.getColumnCount(); i++) {
			if (inputJTable.getModel().getColumnName(i).equals(columnName)) {
				outputInteger = i;
			}
		}
		return outputInteger;
	}

	// Finds the index of a column in a DefaultTableModel by the column's name.
	public static int columnNameIndex(DefaultTableModel inputTable, String columnName) {
		int outputInteger = -1;
		for (int i = 0; i < inputTable.getColumnCount(); i++) {
			if (inputTable.getColumnName(i).equals(columnName)) {
				outputInteger = i;
			}
		}
		return outputInteger;
	}

	/**
	 * Converts the entire content of a JTable into a 2D String array.
	 * The first row of the array contains the column names, and the subsequent rows contain the data.
	 * @param table The JTable to convert.
	 * @return A 2D String array containing the table data, including column names as the first row.
	 */
	public static String[][] jTableToStringArray(JTable table) {
		TableModel model = table.getModel();
		int rowCount = model.getRowCount();
		int columnCount = model.getColumnCount();
		String[][] tableData = new String[rowCount + 1][columnCount];
		for (int j = 0; j < columnCount; j++) {
			tableData[0][j] = model.getColumnName(j); // Column names as the first row
		}
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				Object value = model.getValueAt(i, j);
				tableData[i + 1][j] = (value == null) ? "" : value.toString(); // Convert each value to String
			}
		}
		return tableData;
	}
}