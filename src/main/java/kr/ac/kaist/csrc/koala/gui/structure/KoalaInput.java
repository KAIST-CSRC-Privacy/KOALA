package kr.ac.kaist.csrc.koala.gui.structure;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import kr.ac.kaist.csrc.koala.utils.DataHandle;
import kr.ac.kaist.csrc.koala.utils.db.KoalaDAO;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KoalaInput {

	public String sensitiveValue = "    ";
	public String sensitiveType = "   ";
	private File loadedFile;
	public ArrayList<String> currentQID;
	public ARXConfiguration config;
	public DefaultTableModel query;
	private Data loadedData;
	private String[] columnNames;
	private DefaultTableModel loadedTable;
	private JTable loadedJTable;
	private DataSubset dataSubset;
	public Map<String, String> attributeDataType;

	public KoalaInput(String dbName) {
		try {
			Connection conn = KoalaDAO.makeDBConnect();
			String[][] loadedDB = KoalaDAO.readDBTable(conn, dbName);
            assert conn != null;
            conn.close();
			arrayToTable(loadedDB);
			commonInitialization();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "DB-Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public KoalaInput(File loadedFile) {
		this.loadedFile = loadedFile;
		fileToTable(loadedFile);
		commonInitialization();
	}

	private void commonInitialization() {
		config = ARXConfiguration.create();
		setAllAttributes();
		loadedJTable = new JTable(loadedTable);
		currentQID = new ArrayList<>();
		query = new DefaultTableModel(new String[]{""}, 0);
		attributeDataType = new HashMap<>();
	}

	//Data and DefaultTableModel Initialize
	private void arrayToTable(String[][] loadedDB) {
		loadedData = Data.create(loadedDB);
		int columnNums = loadedData.getHandle().getNumColumns();
		int rowNums = loadedData.getHandle().getNumRows();
		columnNames = new String[columnNums];
		for (int k = 0; k < columnNums; k++) {
			columnNames[k] = loadedData.getHandle().getAttributeName(k);
		}
		loadedTable = new DefaultTableModel(columnNames, rowNums);
		for (int i = 0; i < columnNums; i++) {
			for (int j = 0; j < rowNums; j++) {
				loadedTable.setValueAt(loadedData.getHandle().getValue(j, i), j, i);
			}
		}
	}

	private void fileToTable(File loadedFile) {
		try {
			String[][] data = readCsvFile(loadedFile);
			loadedData = Data.create(data);

			int columnNums = loadedData.getHandle().getNumColumns();
			int rowNums = loadedData.getHandle().getNumRows();
			columnNames = new String[columnNums];
			for (int k = 0; k < columnNums; k++) {
				columnNames[k] = loadedData.getHandle().getAttributeName(k);
			}
			loadedTable = new DefaultTableModel(columnNames, rowNums);
			for (int i = 0; i < columnNums; i++) {
				for (int j = 0; j < rowNums; j++) {
					loadedTable.setValueAt(loadedData.getHandle().getValue(j, i), j, i);
				}
			}
		} catch (IOException | CsvException e) {
			throw new RuntimeException(e);
		}
    }

	private String[][] readCsvFile(File file) throws IOException, CsvException {
		CSVReader reader = new CSVReader(new FileReader(file));
		return reader.readAll().toArray(String[][]::new);
	}

	public DefaultTableModel attributeArrayInit() {
		DefaultTableModel output = new DefaultTableModel(new String[]{"Attribute", "QI or SA", "Data Type"}, 0);
		ArrayList<String[]> temp = new ArrayList<>();
		sortAttribute(temp, AttributeType.SENSITIVE_ATTRIBUTE.toString());
		sortAttribute(temp, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE.toString());
		sortAttribute(temp, AttributeType.INSENSITIVE_ATTRIBUTE.toString());
		sortAttribute(temp, AttributeType.IDENTIFYING_ATTRIBUTE.toString());

		for (String[] i : temp) {
			output.addRow(i);
		}
		return output;
	}

	public DefaultTableModel allAttributeInit() {
		DefaultTableModel output = new DefaultTableModel(new String[]{"Attribute", "QI or SA", "Data Type"}, 0);
		ArrayList<String[]> temp = new ArrayList<>();
		sortAttributeInit(temp);
		for (String[] i : temp) {
			output.addRow(i);
		}
		return output;

	}

	private void sortAttributeInit(ArrayList<String[]> input) {
		for (int i = 0; i < loadedData.getHandle().getNumColumns(); i++) {
			String[] temp = new String[3];
			temp[0] = loadedData.getHandle().getAttributeName(i);
			temp[1] = "INSENSITIVE_ATTRIBUTE";
			if (isDouble(loadedData.getHandle().getValue(0, i))) {
				attributeDataType.put(temp[0], "Numerical");
				temp[2] = "Numerical";
			} else {
				attributeDataType.put(temp[0], "String");
				temp[2] = "String";
			}
			input.add(temp);
		}
	}

	public void sortAttribute(ArrayList<String[]> input, String attributeType) {
		for (int i = 0; i < loadedData.getHandle().getNumColumns(); i++) {
			String[] temp = new String[3];
			temp[0] = loadedData.getHandle().getAttributeName(i);
			temp[1] = loadedData.getDefinition().getAttributeType(temp[0]).toString();
			temp[2] = attributeDataType.get(temp[0]);

			if (temp[1].equals(AttributeType.SENSITIVE_ATTRIBUTE.toString())) {
				sensitiveValue = temp[0];
				sensitiveType = temp[2];
			}
			if (temp[1].equals(attributeType)) {
				input.add(temp);
			}
		}
	}

	private boolean isDouble(String strValue) {
		try {
			Double.parseDouble(strValue);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private String transformAttributeType(String attributeType) {
		if (attributeType.equals("INSENSITIVE_ATTRIBUTE")) {
			return "Insensitive";
		} else if (attributeType.equals("QUASI_IDENTIFYING_ATTRIBUTE")) {
			return "Quasi";
		} else if (attributeType.equals("IDENTIFYING_ATTRIBUTE")) {
			return "Identity";
		} else {
			return "Sensitive";
		}
	}

	private void setAllAttributes() {
		for (int i = 0; i < loadedData.getHandle().getNumColumns(); i++) {
			loadedData.getDefinition().setAttributeType(loadedData.getHandle().getAttributeName(i),
				AttributeType.INSENSITIVE_ATTRIBUTE);
		}
	}

	public void addQID(String qid) {
		if (currentQID.contains(qid)) {
			return;
		}
		currentQID.add(qid);
	}

	public void removeQID(String qid) {
		currentQID.remove(qid);
	}

	public File getLoadedFile() {
		return loadedFile;
	}

	public Data getLoadedData() {
		return loadedData;
	}

	public DefaultTableModel getLoadedTable() {
		return loadedTable;
	}

	public JTable getLoadedJTable() {
		return loadedJTable;
	}

	public String[] getColumnNames() {
		return columnNames;
	}


	public void setDataSubset(DataSubset dataSubset) {
		this.dataSubset = dataSubset;
	}

	public DataSubset getDataSubset() {
		return dataSubset;
	}

	public String[][] getJTableStringArray() {
		return DataHandle.jTableToStringArray(loadedJTable);
	}
}
