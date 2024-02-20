package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.utils.HierarchyHelper;

import javax.swing.*;
import java.awt.*;

import static kr.ac.kaist.csrc.koala.utils.DataHandle.categoricalToNum;

public class HierarchyNumSetting extends JFrame {

	public static JTable tempJTable = new JTable();
	public static JTextField lower = new JTextField(5);
	public static JTextField upper = new JTextField(5);
	public static JTextField interval = new JTextField(5);
	public static JTextField levNum = new JTextField(5);
	public static JTextField levSize = new JTextField(5);
	public static JLabel maxValJLabel = new JLabel("Max : ");
	public static JLabel maxVal = new JLabel();
	public static JLabel minValJLabel = new JLabel("Min : ");
	public static JLabel minVal = new JLabel();
	private JLabel lowerJLabel = new JLabel("Lower: ");
	private JLabel upperJLabel = new JLabel("Upper: ");
	private JLabel intervalJlabel = new JLabel("Interval: ");
	private JLabel levNumJLabel = new JLabel("Number of Level: ");
	private JLabel levSizeJLabel = new JLabel("Level Grouping Size : ");
	private JPanel upperPanel = new JPanel();
	private JPanel upperFirstPanel = new JPanel();
	private JPanel upperSecondPanel = new JPanel();
	private JPanel upperThirdPanel = new JPanel();
	private JPanel upperFourthPanel = new JPanel();
	private JPanel upperFirstPanelFirstRow = new JPanel();
	private JPanel upperFirstPanelSecondRow = new JPanel();
	private JPanel upperSecondPanelFirstRow = new JPanel();
	private JPanel upperSecondPanelSecondRow = new JPanel();
	private JPanel upperSecondPanelThirdRow = new JPanel();
	private JPanel upperThirdPanelFirstRow = new JPanel();
	private JPanel upperThirdPanelSecondRow = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JButton applyButton = new JButton("Apply");
	private JButton confirmButton = new JButton("Confirm");
	private JButton saveButton = new JButton("Save");
	private JButton cancelButton = new JButton("Exit");

	public HierarchyNumSetting(HierarchyHelper.HierarchyData hierarchyData, KoalaInput koalaInput) {
		this.setBounds(100, 100, 800, 600);
		this.setLayout(new BorderLayout());
		this.setVisible(true);

		String[] distinctTemp = koalaInput.getLoadedData().getHandle()
			.getDistinctValues(koalaInput.getLoadedData().getHandle().getColumnIndexOf(hierarchyData.attributeName));
		maxVal.setText(String.valueOf(categoricalToNum(koalaInput.getLoadedData().getHandle().getDistinctValues(
			koalaInput.getLoadedData().getHandle().getColumnIndexOf(hierarchyData.attributeName)))[0]));
		minVal.setText(String.valueOf(categoricalToNum(koalaInput.getLoadedData().getHandle().getDistinctValues(
			koalaInput.getLoadedData().getHandle().getColumnIndexOf(hierarchyData.attributeName)))[1]));

		//Panel layout Settings
		upperPanel.setLayout(new GridLayout(1, 4));
		upperFirstPanel.setLayout(new GridLayout(2, 1));
		upperSecondPanel.setLayout(new GridLayout(3, 1));
		upperThirdPanel.setLayout(new GridLayout(2, 1));
		upperFirstPanelFirstRow.setLayout(new GridLayout(1, 2));
		upperFirstPanelSecondRow.setLayout(new GridLayout(1, 2));
		upperSecondPanelFirstRow.setLayout(new GridLayout(1, 2));
		upperSecondPanelSecondRow.setLayout(new GridLayout(1, 2));
		upperSecondPanelThirdRow.setLayout(new GridLayout(1, 2));
		upperThirdPanelFirstRow.setLayout(new GridLayout(1, 2));
		upperThirdPanelSecondRow.setLayout(new GridLayout(1, 2));
		bottomPanel.setLayout(new GridLayout(1, 3));
		this.add(upperPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		//upperPanel Setting
		upperPanel.add(upperFirstPanel);
		upperPanel.add(upperSecondPanel);
		upperPanel.add(upperThirdPanel);
		upperPanel.add(upperFourthPanel);

		upperFirstPanel.add(upperFirstPanelFirstRow);
		upperFirstPanel.add(upperFirstPanelSecondRow);
		upperSecondPanel.add(upperSecondPanelFirstRow);
		upperSecondPanel.add(upperSecondPanelSecondRow);
		upperSecondPanel.add(upperSecondPanelThirdRow);
		upperThirdPanel.add(upperThirdPanelFirstRow);
		upperThirdPanel.add(upperThirdPanelSecondRow);
		upperFourthPanel.add(applyButton);

		upperFirstPanelFirstRow.add(upperJLabel);
		upperFirstPanelFirstRow.add(upper);
		upperFirstPanelSecondRow.add(lowerJLabel);
		upperFirstPanelSecondRow.add(lower);
		upperSecondPanelFirstRow.add(levNumJLabel);
		upperSecondPanelFirstRow.add(levNum);
		upperSecondPanelSecondRow.add(intervalJlabel);
		upperSecondPanelSecondRow.add(interval);
		upperSecondPanelThirdRow.add(levSizeJLabel);
		upperSecondPanelThirdRow.add(levSize);

		upperThirdPanelFirstRow.add(maxValJLabel);
		upperThirdPanelFirstRow.add(maxVal);
		upperThirdPanelSecondRow.add(minValJLabel);
		upperThirdPanelSecondRow.add(minVal);

		//centerPanel Setting
		centerPanel.add(new JScrollPane(tempJTable));

		//bottomPanel Setting
		bottomPanel.add(confirmButton);
		bottomPanel.add(saveButton);
		bottomPanel.add(cancelButton);

		applyButton.addActionListener(e -> {
            if (Double.parseDouble(maxVal.getText()) < Double.parseDouble(minVal.getText())) {
                return;
            }
            try {
                String[] inputNums = HierarchyHelper.getSampleData(Long.parseLong(lower.getText()),
                    Long.parseLong(upper.getText()));
                HierarchyHelper.HierarchyData hierarchyData2 = new HierarchyHelper.IntegerIntervalHierarchyData(
                    hierarchyData.attributeName,
                    koalaInput.getLoadedData().getDefinition().getAttributeType(hierarchyData.attributeName),
                    inputNums,
                    Long.parseLong(lower.getText()), Long.parseLong(upper.getText()),
                    Long.parseLong(interval.getText()), Integer.parseInt(levNum.getText()),
                    Integer.parseInt(levSize.getText()));
                tempJTable.setModel(hierarchyData2.hierarchyDefaultTable);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "error");
                throw new RuntimeException(ex);
            }
        });

		confirmButton.addActionListener(e -> {
            HierarchyHelper.IntegerIntervalHierarchyData hierarchyData3 = new HierarchyHelper.IntegerIntervalHierarchyData(
                hierarchyData.attributeName, hierarchyData.attrType, distinctTemp,
                Long.parseLong(lower.getText()), Long.parseLong(upper.getText()),
                Long.parseLong(interval.getText()), Integer.parseInt(levNum.getText()),
                Integer.parseInt(levSize.getText()));

            hierarchyData.hierarchyDefaultTable = hierarchyData3.hierarchyDefaultTable;
            hierarchyData.hierarchyJTable.setModel(hierarchyData.hierarchyDefaultTable);
            tempJTable.removeAll();
            dispose();
        });
	}
}
