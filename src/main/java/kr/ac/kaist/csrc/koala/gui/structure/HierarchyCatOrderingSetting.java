package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.utils.DataHandle;
import kr.ac.kaist.csrc.koala.utils.HierarchyHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static kr.ac.kaist.csrc.koala.utils.DataHandle.*;

public class HierarchyCatOrderingSetting extends JFrame {

	private final JPanel levelSettingPanel, levelButtonPanel, levelAddDeletePanel, topLabels;
	private final JButton levelConfirmButton, levelExitButton, levelAddButton, levelDeleteButton, nextLevelButton, confirmAndExitButton;
	private final JScrollPane distinctValuesPane, groupValuesPane, confirmedGroupPane;
	private final JLabel topFirst, topSecond, topThird, topFourth, topFifth, topSixth;
	private DefaultListModel distinctList;
	private DefaultListModel groupList;
	private DefaultListModel confirmedGroupList;
	private final JTextField groupName;
	private JList<String> distinctJList;
	private JList emptyJList;
	private JList<String> confirmedGroupJList;
	private DefaultListModel duplicateList;
	private Map<String, ArrayList<String>> distinctGroup;
	private final Map<String, Integer> distinctNum;
	private final ArrayList<String> distinctCol = new ArrayList<>();
	private final ArrayList<String> groupCol = new ArrayList<>();
	private final ArrayList<String> starCol = new ArrayList<>();
	//reorderTempTable0
	private ArrayList<String> distinctConfirmedGroup = new ArrayList<>();

	public HierarchyCatOrderingSetting(HierarchyHelper.HierarchyData hierarchyData) {
		this.setBounds(100, 100, 1000, 400);
		this.setLayout(new BorderLayout());
		this.setVisible(true);

		//Setting basic labels
		topLabels = new JPanel();
		topLabels.setLayout(new GridLayout(1, 6));

		topFirst = new JLabel("Distinct Values", SwingConstants.CENTER);
		topSecond = new JLabel("Add/Delete", SwingConstants.CENTER);
		topThird = new JLabel("Selected Values", SwingConstants.CENTER);
		topFourth = new JLabel("Group Name", SwingConstants.CENTER);
		topFifth = new JLabel("Confirm Group", SwingConstants.CENTER);
		topSixth = new JLabel("Values - Group", SwingConstants.CENTER);

		topLabels.add(topFirst);
		topLabels.add(topSecond);
		topLabels.add(topThird);
		topLabels.add(topFourth);
		topLabels.add(topFifth);
		topLabels.add(topSixth);

		//button settings
		levelConfirmButton = new JButton("Confirm");
		levelExitButton = new JButton("Exit");
		levelAddButton = new JButton("Add");
		levelDeleteButton = new JButton("Delete");
		nextLevelButton = new JButton("Next Level");
		confirmAndExitButton = new JButton("Confirm & Exit");

		levelSettingPanel = new JPanel();
		levelButtonPanel = new JPanel();
		duplicateList = DataHandle.columnToListModel(hierarchyData.hierarchyJTable, 0);
		groupList = new DefaultListModel();
		confirmedGroupList = new DefaultListModel();
		emptyJList = new JList<>(groupList);
		confirmedGroupJList = new JList<>(confirmedGroupList);
		distinctGroup = new LinkedHashMap<>();
		distinctNum = new LinkedHashMap<>();
		distinctList = setDistinctList(duplicateList);

		for (int i = 0; i < duplicateList.size(); i++) {
			if (!distinctNum.containsKey(duplicateList.get(i).toString())) {
				distinctNum.put(duplicateList.get(i).toString(), 1);
			} else {
				distinctNum.put(duplicateList.get(i).toString(), distinctNum.get(duplicateList.get(i).toString()) + 1);
			}
		}
		distinctJList = new JList<>(distinctList);
		groupName = new JTextField(10);

		groupValuesPane = new JScrollPane(emptyJList);
		confirmedGroupPane = new JScrollPane(confirmedGroupJList);

		levelAddDeletePanel = new JPanel();
		levelAddDeletePanel.setLayout(new GridLayout(2, 1));
		levelAddDeletePanel.add(levelAddButton);
		levelAddDeletePanel.add(levelDeleteButton);
		distinctValuesPane = new JScrollPane(distinctJList);
		levelSettingPanel.setLayout(new GridLayout(1, 6));
		levelSettingPanel.add(distinctValuesPane);
		levelSettingPanel.add(levelAddDeletePanel);
		levelSettingPanel.add(groupValuesPane);
		levelSettingPanel.add(groupName);
		levelSettingPanel.add(levelConfirmButton);
		levelSettingPanel.add(confirmedGroupPane);

		levelButtonPanel.setLayout(new GridLayout(1, 3));
		levelButtonPanel.add(nextLevelButton);
		levelButtonPanel.add(confirmAndExitButton);
		levelButtonPanel.add(levelExitButton);

		this.add(topLabels, BorderLayout.NORTH);
		this.add(levelSettingPanel, BorderLayout.CENTER);
		this.add(levelButtonPanel, BorderLayout.SOUTH);

		// Enable Setting
		levelDeleteButton.setEnabled(false);
		nextLevelButton.setEnabled(false);
		levelConfirmButton.setEnabled(false);
		confirmAndExitButton.setEnabled(false);
		groupName.setEnabled(false);

		levelAddButton.addActionListener(e -> {
            int distinctSize = distinctJList.getSelectedValuesList().size();
            for (int i = 0; i < distinctSize; i++) {
                groupList.addElement(distinctJList.getSelectedValuesList().get(0));
                distinctList.removeElement(distinctJList.getSelectedValuesList().get(0));
            }

            // Enable Setting
            if (!groupList.isEmpty()) {
                levelDeleteButton.setEnabled(true);
            }
            if (distinctList.isEmpty()) {
                levelAddButton.setEnabled(false);
            }
            groupName.setEnabled(true);
            levelConfirmButton.setEnabled(true);
        });
		levelDeleteButton.addActionListener(e -> {
            int deleteSize = emptyJList.getSelectedValuesList().size();
            for (int i = 0; i < deleteSize; i++) {
                distinctList.addElement(emptyJList.getSelectedValuesList().get(0));
                groupList.removeElement(emptyJList.getSelectedValuesList().get(0));
            }

            // Enable Setting
            if (groupList.isEmpty()) {
                levelDeleteButton.setEnabled(false);
                groupName.setEnabled(false);
                levelConfirmButton.setEnabled(false);
            }
            if (!distinctList.isEmpty()) {
                levelAddButton.setEnabled(true);
            }
        });
		levelConfirmButton.addActionListener(e -> {
            if (groupName.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Please Enter a group name");
                return;
            }
            for (int i = 0; i < groupList.getSize(); i++) {
                if (!distinctGroup.containsKey(groupName.getText())) {
                    distinctGroup.put(groupName.getText(), new ArrayList<>());
                }
                for (int j = 0; j < distinctNum.get(groupList.get(i)); j++) {
                    distinctGroup.get(groupName.getText()).add(groupList.get(i).toString());
                    distinctConfirmedGroup.add(groupList.get(i).toString());
                    confirmedGroupList.addElement(groupList.get(i).toString() + " " + groupName.getText());
                }
            }
            groupList.removeAllElements();
            groupName.setText("");

            // Enable Setting
            groupName.setEnabled(false);
            levelConfirmButton.setEnabled(false);
            levelDeleteButton.setEnabled(false);
            if (distinctList.isEmpty()) {
                nextLevelButton.setEnabled(true);
                confirmAndExitButton.setEnabled(true);
            }
        });
		nextLevelButton.addActionListener(e -> hierarchyTableConstructor(hierarchyData));

		levelExitButton.addActionListener(e -> dispose());
		confirmAndExitButton.addActionListener(e -> {
            hierarchyTableConstructor(hierarchyData);
            dispose();
        });
	}

	private void hierarchyTableConstructor(HierarchyHelper.HierarchyData hierarchyData) {

		distinctList.removeAllElements();
		groupList.removeAllElements();
		duplicateList.removeAllElements();
		distinctNum.clear();

		hierarchyData.hierarchyDefaultTable.setColumnCount(hierarchyData.hierarchyDefaultTable.getColumnCount() + 1);
		DefaultTableModel reorderTempTable = new DefaultTableModel(
			getColumnNameDefaultTable(hierarchyData.hierarchyDefaultTable), 0);

		for (String j : getDistinctArray(distinctConfirmedGroup)) {
			for (int i = 0; i < hierarchyData.hierarchyDefaultTable.getRowCount(); i++) {
				if (hierarchyData.hierarchyDefaultTable.getValueAt(i,
					hierarchyData.hierarchyDefaultTable.getColumnCount() - 3) == j) {
					reorderTempTable.addRow(getRowDefaultTable(hierarchyData.hierarchyDefaultTable, i));
				}
			}
		}
		hierarchyData.hierarchyDefaultTable = defaultTableModelClone(reorderTempTable);
		hierarchyData.hierarchyJTable.setModel(hierarchyData.hierarchyDefaultTable);
		setHierarchyTableHeader(hierarchyData.hierarchyJTable);

		confirmedGroupList.removeAllElements();

		for (String i : distinctGroup.keySet()) {
			for (String j : distinctGroup.get(i)) {
				distinctCol.add(j);
				groupCol.add(i);
				starCol.add("*");
			}
		}
		setColumnArrayList(hierarchyData, distinctCol, hierarchyData.hierarchyDefaultTable.getColumnCount() - 3);
		setColumnArrayList(hierarchyData, groupCol, hierarchyData.hierarchyDefaultTable.getColumnCount() - 2);
		setColumnArrayList(hierarchyData, starCol, hierarchyData.hierarchyDefaultTable.getColumnCount() - 1);

		duplicateList = ArrayListToList(groupCol);
		distinctList = setDistinctList(duplicateList);
		distinctJList = new JList<>(distinctList);
		distinctValuesPane.setViewportView(distinctJList);
		emptyJList = new JList<>(groupList);
		groupValuesPane.setViewportView(emptyJList);
		confirmedGroupJList = new JList<>(confirmedGroupList);
		confirmedGroupPane.setViewportView(confirmedGroupJList);
		distinctCol.clear();
		groupCol.clear();
		starCol.clear();
		distinctGroup.clear();
		distinctConfirmedGroup.clear();

		for (int i = 0; i < duplicateList.size(); i++) {
			if (!distinctNum.containsKey(duplicateList.get(i).toString())) {
				distinctNum.put(duplicateList.get(i).toString(), 1);
			} else {
				distinctNum.put(duplicateList.get(i).toString(), distinctNum.get(duplicateList.get(i).toString()) + 1);
			}
		}

		// Enable Setting
		levelAddButton.setEnabled(true);
		levelDeleteButton.setEnabled(false);
		nextLevelButton.setEnabled(false);
		levelConfirmButton.setEnabled(false);
		confirmAndExitButton.setEnabled(false);
		groupName.setEnabled(false);
	}
}
