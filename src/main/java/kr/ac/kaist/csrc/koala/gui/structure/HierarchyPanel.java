package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.utils.HierarchyHelper.DefaultHierarchyData;
import kr.ac.kaist.csrc.koala.utils.HierarchyHelper.HierarchyData;
import org.deidentifier.arx.Data;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static kr.ac.kaist.csrc.koala.utils.DataHandle.*;

/**
 * @author Yongki Hong
 */

public class HierarchyPanel {

	KoalaHierarchy koalaHierarchy;
	public JPanel taxonomyTreePanel, buttonPanel;
	public JPanel optionPane = new JPanel();
    JScrollPane hierarchyScrollPane, hierarchySummaryPane;
	public HierarchyData initialHierarchyData;
	public static JTable hierarchyAttributeJTable;
	public static JTable hierarchySummaryJTable;
    JButton hierarchyLoadButton = new JButton("Load Tree");
	JButton sensitivitySettingButton = new JButton("Sensitivity");

	public HierarchyPanel(KoalaInput koalaInput, JButton anonymizationButton) {
		koalaHierarchy = new KoalaHierarchy(koalaInput);

		// Panel layout settings
		taxonomyTreePanel = new JPanel(new GridLayout(1, 2));
		taxonomyTreePanel.setPreferredSize(new Dimension(950, 160));

		// Initialize JTables for hierarchy attributes and summary
		hierarchyAttributeJTable = new JTable();
		hierarchySummaryJTable = new JTable();
		hierarchyScrollPane = new JScrollPane(hierarchyAttributeJTable);
		hierarchySummaryPane = new JScrollPane(hierarchySummaryJTable);

		// Set models for JTables
		hierarchyAttributeJTable.setModel(koalaHierarchy.hierarchyAttributeTable);
		hierarchySummaryJTable.setModel(koalaHierarchy.hierarchyTreeTable);

		// Configure selection modes for tables
		hierarchyAttributeJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Initialize panels for layout
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		JPanel rightPanel = new JPanel(new GridLayout(1, 1));

		// Add components to left panel
		buttonPanel = new JPanel();
		JButton defaultTreeBtn = new JButton("Default Tree");
		JButton hierarchySettingButton = new JButton("New Tree");
		buttonPanel.add(defaultTreeBtn);
		buttonPanel.add(hierarchySettingButton);
		buttonPanel.add(hierarchyLoadButton);
		buttonPanel.add(sensitivitySettingButton);
		leftPanel.add(hierarchyScrollPane);
		leftPanel.add(buttonPanel);

		// Set borders with titles for panels
		setPanelBorders(leftPanel, rightPanel);

		// Add hierarchy summary to right panel
		rightPanel.add(hierarchySummaryPane);

		// Configure the bottom panel in the east section
		JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.add(anonymizationButton, BorderLayout.SOUTH);

		// Add panels to the taxonomy tree panel
		taxonomyTreePanel.add(leftPanel);
		taxonomyTreePanel.add(rightPanel);
		taxonomyTreePanel.add(btnPanel);

		// OptionPane Settings
        GridLayout numGrid = new GridLayout(7, 2);
        optionPane.setLayout(numGrid);

		//Enable Settings

		hierarchyAttributeJTable.getTableHeader().setEnabled(false);
		hierarchyAttributeJTable.setRowSelectionAllowed(true);

		hierarchySummaryJTable.getTableHeader().setEnabled(false);
		hierarchySummaryJTable.setRowSelectionAllowed(true);

		//Add Panel to MainFrame

		hierarchySettingButton.addActionListener(e -> {
            try {
                int rowIndex = hierarchyAttributeJTable.getSelectedRow();
                String attributeName = hierarchyAttributeJTable.getValueAt(rowIndex, 0).toString();
                String[] distinctValues = koalaInput.getLoadedData().getHandle()
                    .getDistinctValues(koalaInput.getLoadedData().getHandle().getColumnIndexOf(attributeName));

                HierarchyData hierarchyData = new DefaultHierarchyData(attributeName, distinctValues,
                    koalaInput.getLoadedData().getDefinition().getAttributeType(attributeName));
                initialHierarchyData = new DefaultHierarchyData(attributeName, distinctValues,
                    koalaInput.getLoadedData().getDefinition().getAttributeType(attributeName));
                hierarchyData.attributeFormat = "String";
                new HierarchyFrame(hierarchyData, koalaInput);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Select an Attribute");
            }
        });

		hierarchyLoadButton.addActionListener(e -> {
            int rowIndex = hierarchyAttributeJTable.getSelectedRow();
            String attributeName = hierarchyAttributeJTable.getValueAt(rowIndex, 0).toString();

            if (!koalaInput.getLoadedData().getDefinition().isHierarchyAvailable(attributeName)) {
                JOptionPane.showMessageDialog(null, "'" + attributeName + "' has no saved hierarchy.");
                JOptionPane.getRootFrame().dispose();
            } else {
                HierarchyData hierarchyData = new HierarchyData(attributeName,
                    koalaInput.getLoadedData().getDefinition().getAttributeType(attributeName));
                hierarchyData.loadExistOHierarchy(koalaInput.getLoadedData());
                new HierarchyFrame(hierarchyData, koalaInput);
            }
        });

		sensitivitySettingButton.addActionListener(e -> new SensitiveSetting(getDistinctArray(
            getColumnString(dataToJTable(koalaInput.getLoadedData()), koalaInput.sensitiveValue))));
	}

	public static DefaultTableModel getHierarchySummaryTable(Data inputData) {
		String[] hierarchyJTableColumn = {"Attribute", "Domain Size", "Number of Tree Levels"};
		DefaultTableModel outputTable = new DefaultTableModel(hierarchyJTableColumn, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		if (inputData == null) {
			return new DefaultTableModel(new String[]{"A", "B"}, 0);
		}

		for (int i = 0; i < inputData.getHandle().getNumColumns(); i++) {
			String attribute = inputData.getHandle().getAttributeName(i);
			if (inputData.getDefinition().isHierarchyAvailable(attribute)) {
				String[] hierarchySummaryRow = {attribute,
					String.valueOf(inputData.getDefinition().getHierarchy(attribute).length),
					String.valueOf(inputData.getDefinition().getHierarchy(attribute)[0].length)};
				outputTable.addRow(hierarchySummaryRow);
			}
		}
		return outputTable;
	}

	private void setHierarchySummaryJTable(JTable inputJTable) {
		String[] hierarchyTableColumn = {"Attribute", "QI or SA", "Data Type", "Default Tree"};
		DefaultTableModel table;
		if (inputJTable == null) {
			table = new DefaultTableModel(new String[]{"A", "B"}, 0);
			return;
		}
		table = new DefaultTableModel(hierarchyTableColumn, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		for (int i = 0; i < inputJTable.getRowCount(); i++) {
			if (inputJTable.getValueAt(i, 1).equals("QI")) {
				String[] hierarchyTableRow = new String[3];
				hierarchyTableRow[0] = (String) inputJTable.getValueAt(i, 0);
				hierarchyTableRow[1] = (String) inputJTable.getValueAt(i, 1);
				hierarchyTableRow[2] = (String) inputJTable.getValueAt(i, 2);
				table.addRow(hierarchyTableRow);
			}
		}
		for (int i = 0; i < inputJTable.getRowCount(); i++) {
			if (inputJTable.getValueAt(i, 1).equals("SA")) {
				String[] hierarchyTableRow = new String[3];
				hierarchyTableRow[0] = (String) inputJTable.getValueAt(i, 0);
				hierarchyTableRow[1] = (String) inputJTable.getValueAt(i, 1);
				hierarchyTableRow[2] = (String) inputJTable.getValueAt(i, 2);
				table.addRow(hierarchyTableRow);
			}
		}
		hierarchyAttributeJTable.setModel(table);
	}
	private void setPanelBorders(JPanel leftPanel, JPanel rightPanel) {
		TitledBorder border = BorderFactory.createTitledBorder("  [Build a Tree for Each Attribute...]  ");
		border.setTitleJustification(TitledBorder.CENTER);
		leftPanel.setBorder(border);

		border = BorderFactory.createTitledBorder("  [Taxonomy Tree Summary]  ");
		rightPanel.setBorder(border);
	}
}