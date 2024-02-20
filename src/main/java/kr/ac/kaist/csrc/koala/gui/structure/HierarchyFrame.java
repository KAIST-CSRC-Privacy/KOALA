
package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.utils.HierarchyHelper.HierarchyData;
import org.deidentifier.arx.AttributeType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static kr.ac.kaist.csrc.koala.gui.structure.HierarchyPanel.getHierarchySummaryTable;
import static kr.ac.kaist.csrc.koala.gui.structure.HierarchyPanel.hierarchySummaryJTable;
import static kr.ac.kaist.csrc.koala.utils.DataHandle.getColumnString;
import static kr.ac.kaist.csrc.koala.utils.DataHandle.setHierarchyTableHeader;
import static kr.ac.kaist.csrc.koala.utils.HierarchyHelper.hierarchyDatatoHierarchy;

public class HierarchyFrame extends JFrame {

	private JPopupMenu tablePopupMenu = new JPopupMenu();
	private JPanel hierarchyButtonPanel = new JPanel();
	private JMenuItem menuRowAdd = new JMenuItem("Add new Row");
	private JMenuItem menuRowRemove = new JMenuItem("Remove Current Row");
	private JMenuItem menuColAdd = new JMenuItem("Add new Column");
	private JMenuItem menuColRemove = new JMenuItem("Remove Current Column");
	private JButton hierarchyConfirmButton = new JButton("Confirm");
	private JButton levelSettingButton = new JButton("Level Setting");
	private JButton hierarchyResetButton = new JButton("Reset");
	private String[] catOption = {"Ordering", "Masking", "Cancel"};
	private String[] align = {"Left", "Right"};
	private String[] maskFrom = {"Right to Left", "Left to Right"};
	private String[] emptyString = {" ", "O", "X"};
	private String[] maskString = {"*", "-", "_"};
	private JComboBox alignBox = new JComboBox(align);
	private JComboBox maskBox = new JComboBox(maskFrom);
	private JComboBox emptyBox = new JComboBox(emptyString);
	private JComboBox maskStringBox = new JComboBox(maskString);
	private JPanel maskSettingPanel = new JPanel();
	public static String[] sensitiveFirstCol;
	public static String[] sensitiveSecondCol;
	public static Map<String, String> sensitiveMap;

	public HierarchyFrame(HierarchyData hierarchyData, KoalaInput koalaInput) {
		this.setTitle(hierarchyData.attributeName + " Hierarchy Setting");
		this.setBounds(100, 100, 600, 400);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(hierarchyData.hierarchyJTable), BorderLayout.CENTER);
		this.add(hierarchyButtonPanel, BorderLayout.SOUTH);
		hierarchyButtonPanel.setLayout(new GridLayout(1, 3));
		hierarchyButtonPanel.add(hierarchyConfirmButton);
		hierarchyButtonPanel.add(hierarchyResetButton);
		hierarchyButtonPanel.add(levelSettingButton);
		this.setVisible(true);

		tablePopupMenu.add(menuRowAdd);
		tablePopupMenu.add(menuColAdd);
		tablePopupMenu.add(menuRowRemove);
		tablePopupMenu.add(menuColRemove);
		maskSettingPanel.add(alignBox);
		maskSettingPanel.add(maskBox);
		maskSettingPanel.add(emptyBox);
		maskSettingPanel.add(maskStringBox);

		ActionListener levelSetting = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/// Action Needed: remove choice 1 later
				if (koalaInput.attributeDataType.get(hierarchyData.attributeName).equals("Numerical")) {
					new HierarchyNumSetting(hierarchyData, koalaInput);
				} else {
					int catChoice = JOptionPane.showOptionDialog(null, "Categorical Hierarchy Setting",
						"Choose a Hierarchy Option", 0, 0, null, catOption, catOption[2]);
					if (catChoice == 0) {
						new HierarchyCatOrderingSetting(hierarchyData);
					}
					if (catChoice == 1) {
						int reply = JOptionPane.showConfirmDialog(null, maskSettingPanel, "Masking Setting",
							JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (reply == JOptionPane.OK_OPTION) {
							maskingSetting(hierarchyData, Objects.requireNonNull(alignBox.getSelectedItem()).toString(),
								Objects.requireNonNull(maskBox.getSelectedItem()).toString(),
								Objects.requireNonNull(emptyBox.getSelectedItem()).toString(),
								Objects.requireNonNull(maskStringBox.getSelectedItem()).toString());
						}
					}
					if (catChoice == 2) {
						dispose();
					}
				}
			}
		};
		ActionListener hierarchyConfirm = e -> {
            hierarchyData.hierarchy = hierarchyDatatoHierarchy(hierarchyData);
            koalaInput.getLoadedData().getDefinition()
                .setAttributeType(hierarchyData.attributeName, hierarchyData.hierarchy);
            koalaInput.getLoadedData().getDefinition()
                .setAttributeType(hierarchyData.attributeName, hierarchyData.attrType);

            levelSettingButton.removeActionListener(levelSetting);
            hierarchySummaryJTable.setModel(getHierarchySummaryTable(koalaInput.getLoadedData()));

            if (koalaInput.getLoadedData().getDefinition().getAttributeType(hierarchyData.attributeName)
                .equals(AttributeType.SENSITIVE_ATTRIBUTE)) {
                sensitiveFirstCol = getColumnString(hierarchyData.hierarchyJTable, 0);
                sensitiveSecondCol = getColumnString(hierarchyData.hierarchyJTable, 1);
                sensitiveMap = new HashMap<>();
                for (int i = 0; i < sensitiveSecondCol.length; i++) {
                    sensitiveMap.put(sensitiveFirstCol[i], sensitiveSecondCol[i]);
                }
            }
            repaint();
            dispose();
        };
		hierarchyResetButton.addActionListener(e -> {
            hierarchyData.hierarchyJTable.setModel(hierarchyData.hierarchyDefaultTable);
            setHierarchyTableHeader(hierarchyData.hierarchyJTable);
        });

		hierarchyConfirmButton.addActionListener(hierarchyConfirm);
		levelSettingButton.addActionListener(levelSetting);
	}

	public void maskingSetting(HierarchyData hierarchyData, String align, String maskFrom, String emptyString,
		String maskString) {
		int maxLength = 0;
		for (int i = 0; i < hierarchyData.hierarchyJTable.getRowCount(); i++) {
			if (hierarchyData.hierarchyJTable.getValueAt(i, 0).toString().length() > maxLength) {
				maxLength = hierarchyData.hierarchyJTable.getValueAt(i, 0).toString().length();
			}
		}
		DefaultTableModel maskedTable = new DefaultTableModel(hierarchyData.hierarchyJTable.getRowCount(),
			maxLength + 1);
		for (int i = 0; i < hierarchyData.hierarchyJTable.getRowCount(); i++) {
			maskedTable.setValueAt(hierarchyData.hierarchyJTable.getValueAt(i, 0), i, 0);
		}
		if (align.equals("Left")) {
			for (int i = 0; i < maskedTable.getRowCount(); i++) {
				int tempLength = maskedTable.getValueAt(i, 0).toString().length();
				for (int j = 0; j < maxLength - tempLength; j++) {
					maskedTable.setValueAt(maskedTable.getValueAt(i, 0) + emptyString, i, 0);
				}
			}
		}
		if (align.equals("Right")) {
			for (int i = 0; i < maskedTable.getRowCount(); i++) {
				int tempLength = maskedTable.getValueAt(i, 0).toString().length();
				for (int j = 0; j < maxLength - tempLength; j++) {
					maskedTable.setValueAt(emptyString + maskedTable.getValueAt(i, 0), i, 0);
				}
			}
		}

		if (maskFrom.equals("Right to Left")) {
			for (int i = 0; i < maskedTable.getRowCount(); i++) {
				for (int j = 0; j < maxLength; j++) {
					if (j != 0) {
						maskedTable.setValueAt(
							maskedTable.getValueAt(i, j).toString().substring(0, maxLength - 1 - j) + maskString +
								maskedTable.getValueAt(i, j).toString().substring(maxLength - j), i, j + 1);
					} else {
						maskedTable.setValueAt(
							maskedTable.getValueAt(i, j).toString().substring(0, maxLength - 1 - j) + maskString, i,
							j + 1);
					}
				}
			}
		}
		if (maskFrom.equals("Left to Right")) {
			for (int i = 0; i < maskedTable.getRowCount(); i++) {
				for (int j = 0; j < maxLength; j++) {
					if (j != 0) {
						maskedTable.setValueAt(maskedTable.getValueAt(i, j).toString().substring(0, j) + maskString +
							maskedTable.getValueAt(i, j).toString().substring(j + 1), i, j + 1);
					} else {
						maskedTable.setValueAt(maskString + maskedTable.getValueAt(i, j).toString().substring(j + 1), i,
							j + 1);
					}
				}
			}
		}
		for (int i = 0; i < hierarchyData.hierarchyJTable.getRowCount(); i++) {
			maskedTable.setValueAt(hierarchyData.hierarchyJTable.getValueAt(i, 0), i, 0);
		}

		hierarchyData.hierarchyDefaultTable = maskedTable;
		hierarchyData.hierarchyJTable.setModel(hierarchyData.hierarchyDefaultTable);
		setHierarchyTableHeader(hierarchyData.hierarchyJTable);
	}
}