package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.ExprData;
import org.deidentifier.arx.AttributeType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static kr.ac.kaist.csrc.koala.Mainframe.eastPanel;
import static kr.ac.kaist.csrc.koala.gui.structure.HierarchyPanel.hierarchyAttributeJTable;

public class DataAttributePanel {

	//Panels
	public JPanel dataAttributePanel, attributeSettingButtonPanel, resultPanel;
	//Buttons
	private final JButton qiButton = new JButton("Quasi-Identifier (QI)");
	private final JButton saButton = new JButton("Sensitive Attribute (SA)");
	private final JButton idButton = new JButton("Explicit-Identifier (EID)");
	private final JButton nonSaButton = new JButton("Non-sensitive Attribute (NSA)");
	private final JButton categoricalButton = new JButton("Categorical");
	private final JButton numericalButton = new JButton("Numerical");
	private final JButton dateButton = new JButton("Date");
	private final JButton resetButton = new JButton("Reset");
	private final JButton finishBtn = new JButton("Finish");
	public static JTable attributeSettingJTable;
	JScrollPane attributeSettingJScroll;

	public DataAttributePanel(KoalaInput koalaInput) {
		attributeSettingJTable = new JTable();

		// Add the panel to MainFrame
		TitledBorder border;

		dataAttributePanel = new JPanel();
		dataAttributePanel.setLayout(new GridLayout(1, 2));
		dataAttributePanel.setPreferredSize(new Dimension(950, 130));
		border = BorderFactory.createTitledBorder("  [Attribute List]  ");
		border.setTitleJustification(TitledBorder.CENTER);
		dataAttributePanel.setBorder(border);

		// Setting JTable Panel
		attributeSettingJScroll = new JScrollPane(attributeSettingJTable);
		attributeSettingJTable.setModel(koalaInput.allAttributeInit());
		dataAttributePanel.add(attributeSettingJScroll);
		attributeSettingButtonPanel = new JPanel();
		attributeSettingButtonPanel.setLayout(new GridLayout(5, 3, 50, 7));
		attributeSettingButtonPanel.setPreferredSize(new Dimension(950, 130));
		border = BorderFactory.createTitledBorder("  [Attribute Setting Button]  ");
		border.setTitleJustification(TitledBorder.CENTER);
		attributeSettingButtonPanel.setBorder(border);

		JPanel p1 = new JPanel();
		p1.add(new JLabel(("== QI or SA ==")));
		JPanel p2 = new JPanel();
		p2.add(new JLabel(("== Data Type ==")));
		JPanel p3 = new JPanel();
		p3.add(new JLabel(("== Finish or Reset ==")));

		attributeSettingButtonPanel.add(p1);
		attributeSettingButtonPanel.add(p2);
		attributeSettingButtonPanel.add(p3);

		attributeSettingButtonPanel.add(qiButton);
		attributeSettingButtonPanel.add(categoricalButton);
		attributeSettingButtonPanel.add(new JLabel(""));

		attributeSettingButtonPanel.add(saButton);
		attributeSettingButtonPanel.add(numericalButton);
		attributeSettingButtonPanel.add(finishBtn);

		attributeSettingButtonPanel.add(idButton);
		attributeSettingButtonPanel.add(dateButton);
		attributeSettingButtonPanel.add(resetButton);

		attributeSettingButtonPanel.add(nonSaButton);

		resultPanel = new JPanel();
		resultPanel.setLayout(new GridLayout(1, 2));
		resultPanel.add(dataAttributePanel);
		resultPanel.add(attributeSettingButtonPanel);

		qiButton.addActionListener(e -> attributeTypeSetting(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE, koalaInput));
		saButton.addActionListener(e -> attributeTypeSetting(AttributeType.SENSITIVE_ATTRIBUTE, koalaInput));
		nonSaButton.addActionListener(e -> attributeTypeSetting(AttributeType.INSENSITIVE_ATTRIBUTE, koalaInput));
		idButton.addActionListener(e -> attributeTypeSetting(AttributeType.IDENTIFYING_ATTRIBUTE, koalaInput));
		categoricalButton.addActionListener(e -> dataTypeSetting("String", koalaInput));
		numericalButton.addActionListener(e -> dataTypeSetting("Numerical", koalaInput));
		dateButton.addActionListener(e -> dataTypeSetting("Date", koalaInput));
		resetButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null, "Reset All Attribute Setting",
                "Warning", JOptionPane.OK_CANCEL_OPTION);
            if (choice == 0) {
                ExprData.initData();
            }
        });
		finishBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null, "Did you finish the attribute setting?",
                "Warning", JOptionPane.OK_CANCEL_OPTION);
            if (choice == 0) {
            } else {
                JOptionPane.showMessageDialog(null, "Please finish the attribute setting first?");
            }
        });
	}

	private void attributeTypeSetting(AttributeType typeInput, KoalaInput koalaInput) {
		String selectedQID;
		if (attributeSettingJTable.getSelectedRows().length == 0) {
			JOptionPane.showMessageDialog(null, "Please select at least one row.");
		} else {
			for (int i : attributeSettingJTable.getSelectedRows()) {
				koalaInput.getLoadedData().getDefinition()
					.setAttributeType((String) attributeSettingJTable.getValueAt(i, 0),
						typeInput);
			}

			selectedQID = (String) attributeSettingJTable.getValueAt(attributeSettingJTable.getSelectedRow(), 0);

			if (typeInput.toString().equals(AttributeType.QUASI_IDENTIFYING_ATTRIBUTE.toString())) {
				koalaInput.addQID(selectedQID);
			} else {
				koalaInput.currentQID.remove(selectedQID);
			}
			attributeSettingJTable.setModel(koalaInput.attributeArrayInit());
			hierarchyAttributeJTable.setModel(koalaInput.attributeArrayInit());
		}
		eastPanelUpdate(koalaInput);
	}

	private void dataTypeSetting(String dataType, KoalaInput koalaInput) {
		if (attributeSettingJTable.getSelectedRows().length == 0) {
			JOptionPane.showMessageDialog(null, "Please select at least one row.");
		} else {
			for (int i : attributeSettingJTable.getSelectedRows()) {
				koalaInput.attributeDataType.replace((String) attributeSettingJTable.getValueAt(i, 0), dataType);
			}
			attributeSettingJTable.setModel(koalaInput.attributeArrayInit());
			hierarchyAttributeJTable.setModel(koalaInput.attributeArrayInit());
		}
	}

	public void eastPanelUpdate(KoalaInput koalaInput) {
		JPanel temp2 = new JPanel();
		JTable tempJ = new JTable();
		JTable tempJ2 = new JTable();
		DefaultTableModel qidTable = new DefaultTableModel(0, 0);
		DefaultTableModel sensitiveTable = new DefaultTableModel(0, 0);
		qidTable.addColumn("QID", koalaInput.currentQID.toArray());
		sensitiveTable.addColumn("Sen", new String[]{koalaInput.sensitiveValue});
		tempJ.setModel(qidTable);
		tempJ2.setModel(sensitiveTable);
		temp2.setLayout(new GridLayout(1, 2));
		temp2.add(tempJ);
		temp2.add(tempJ2);

		// eastPanel update
		eastPanel.removeAll();  // remove all components
		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(temp2, BorderLayout.CENTER);

		eastPanel.revalidate();
		eastPanel.repaint();
	}
}
