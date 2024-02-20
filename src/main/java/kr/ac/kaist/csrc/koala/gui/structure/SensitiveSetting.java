package kr.ac.kaist.csrc.koala.gui.structure;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static kr.ac.kaist.csrc.koala.ExprData.sensitiveValueList;
import static kr.ac.kaist.csrc.koala.ExprData.sensitivityList;
import static kr.ac.kaist.csrc.koala.utils.DataHandle.arrayToListModel;

public class SensitiveSetting extends JFrame {

	public static DefaultTableModel sensitivityTable;
	public static ArrayList<String> sensitiveArray = new ArrayList<>();
	private JList<String> sensitiveValueJList, sensitivityJList;
	private JLabel topFirstLabel, topSecondLabel, topThirdLabel;
	private JPanel basePanel, listPanel, sensitivityButtonPanel, settingButtonPanel, topPanel;
	private JButton sensitiveSetButton, insensitiveSetButton, sensitivityConfirmButton, saveButton, cancelButton;

	public SensitiveSetting(ArrayList<String> inputArrayList) {
		this.setBounds(100, 100, 600, 300);
		this.setLayout(new BorderLayout());
		this.setVisible(true);

		if (sensitiveValueList.isEmpty()) {
			sensitiveValueList = arrayToListModel(inputArrayList);
			for (int i = 0; i < sensitiveValueList.size(); i++) {
				sensitivityList.addElement("Insensitive");
			}
		}

		sensitiveSetButton = new JButton("Sensitive");
		insensitiveSetButton = new JButton("Insensitive");
		sensitivityConfirmButton = new JButton("Confirm");
		saveButton = new JButton("Save");
		cancelButton = new JButton("Exit");

		topPanel = new JPanel();
		listPanel = new JPanel();
		basePanel = new JPanel();
		settingButtonPanel = new JPanel();
		sensitivityButtonPanel = new JPanel();

		basePanel.setLayout(new BorderLayout());

		topPanel.setLayout(new GridLayout(1, 3));

		topFirstLabel = new JLabel("Distinct Sensitive Values", SwingConstants.CENTER);
		topSecondLabel = new JLabel("Sensitive / Insensitive", SwingConstants.CENTER);
		topThirdLabel = new JLabel("Sensitivity Setting", SwingConstants.CENTER);

		topPanel.add(topFirstLabel);
		topPanel.add(topSecondLabel);
		topPanel.add(topThirdLabel);

		// listPanel Setting
		listPanel.setLayout(new GridLayout(1, 3));
		sensitiveValueJList = new JList<>(sensitiveValueList);
		sensitivityJList = new JList<>(sensitivityList);
		listPanel.add(sensitiveValueJList);
		listPanel.add(sensitivityJList);
		sensitivityJList.setBorder(BorderFactory.createLineBorder(Color.black));
		sensitiveValueJList.setBorder(BorderFactory.createLineBorder(Color.black));

		sensitivityButtonPanel.setLayout(new GridLayout(2, 1));
		sensitivityButtonPanel.add(sensitiveSetButton);
		sensitivityButtonPanel.add(insensitiveSetButton);
		listPanel.add(sensitivityButtonPanel);

		sensitiveValueJList.setEnabled(false);

		//settingButtonPanel Setting
		settingButtonPanel.setLayout(new GridLayout(1, 3));
		settingButtonPanel.add(sensitivityConfirmButton);
		settingButtonPanel.add(saveButton);
		settingButtonPanel.add(cancelButton);

		basePanel.add(topPanel, BorderLayout.NORTH);
		basePanel.add(listPanel, BorderLayout.CENTER);
		basePanel.add(settingButtonPanel, BorderLayout.SOUTH);

		this.add(basePanel);

		sensitiveSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int[] sensitiveSelected = sensitivityJList.getSelectedIndices();
				if (sensitiveSelected.length == 0) {
					JOptionPane.showMessageDialog(null, "Please select at least one row.");
					return;
				}
				int senCount = 0;
				for (String i : sensitivityJList.getSelectedValuesList()) {
					if (i.equals("Insensitive")) {
						senCount++;
					}
				}

				for (int i = 0; i < senCount; i++) {
					sensitivityList.add(0, "Sensitive");
					sensitivityList.remove(sensitivityList.getSize() - 1);
				}
				String[] sen = new String[sensitiveSelected.length];
				for (int i = 0; i < sensitiveSelected.length; i++) {
					sen[i] = (String) sensitiveValueList.get(sensitiveSelected[i]);
					sensitiveArray.remove(sen[i]);
					sensitiveArray.add(sen[i]);
				}

				for (int i = 0; i < sensitiveSelected.length; i++) {
					sensitiveValueList.removeElement(sen[i]);
					sensitiveValueList.add(0, sen[i]);
				}
				sensitiveValueJList.setModel(sensitiveValueList);
				sensitivityJList.setModel(sensitivityList);
			}
		});

		insensitiveSetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int[] insensitiveSelected = sensitivityJList.getSelectedIndices();
				if (insensitiveSelected.length == 0) {
					JOptionPane.showMessageDialog(null, "Please select at least one row.");
					return;
				}
				int senCount = 0;
				for (String i : sensitivityJList.getSelectedValuesList()) {
					if (i.equals("Sensitive")) {
						senCount++;
					}
				}

				for (int i = 0; i < senCount; i++) {
					sensitivityList.addElement("Insensitive");
					sensitivityList.remove(0);
				}
				String[] sen = new String[insensitiveSelected.length];
				for (int i = 0; i < insensitiveSelected.length; i++) {
					sen[i] = (String) sensitiveValueList.get(insensitiveSelected[i]);
					sensitiveArray.remove(sen[i]);
				}

				for (int i = 0; i < insensitiveSelected.length; i++) {
					sensitiveValueList.removeElement(sen[i]);
					sensitiveValueList.addElement(sen[i]);
				}
				sensitiveValueJList.setModel(sensitiveValueList);
				sensitivityJList.setModel(sensitivityList);
			}
		});
		sensitivityConfirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sensitivityTable = new DefaultTableModel(new String[]{"Sensitive Value", "Sensitivity"}, 0);
				for (int i = 0; i < sensitivityList.getSize(); i++) {
					if (sensitivityList.get(i).equals("Sensitive")) {
						sensitivityTable.addRow(
							new String[]{(String) sensitiveValueList.get(i), (String) sensitivityList.get(i)});
					}
				}
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
}
