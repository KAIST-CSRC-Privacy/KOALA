package kr.ac.kaist.csrc.koala.gui.structure;

import org.deidentifier.arx.Data;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QueryFrame {
	Data data;
	DefaultTableModel originalTable;
	DefaultTableModel sortedTable;
	JTable originalJTable;
	JTable sortedJTable;
	JTextField queryTextField;
	JButton applyButton;
	JFrame queryFrame = new JFrame("Query Frame");
	JTabbedPane tabbedPane = new JTabbedPane();
	QueryTableBuilder queryTableBuilder;
	int originalRow, originalCol, sortedRow, sortedCol;
	KoalaInput koalaInput;

	public QueryFrame(KoalaInput koalaInput) {
		this.koalaInput = koalaInput;
		data = koalaInput.getLoadedData();
		originalTable = koalaInput.getLoadedTable();
		originalRow = originalTable.getRowCount();
		originalCol = originalTable.getColumnCount();
		originalJTable = new JTable(originalTable);

		sortedTable = koalaInput.query;

		JScrollPane originalScrollPane = new JScrollPane(originalJTable);
		tabbedPane.addTab("Original Table", originalScrollPane);

		if (sortedTable.getRowCount() == 0) {
			sortedRow = 0;
			sortedCol = 0;
		} else {
			sortedRow = sortedTable.getRowCount();
			sortedCol = sortedTable.getColumnCount();
			sortedJTable.setModel(sortedTable);
		}

		JScrollPane sortedTablePanel = new JScrollPane(sortedJTable);
		tabbedPane.addTab("Sorted Table", sortedTablePanel);

		if (sortedTable.getRowCount() == 0) {
			tabbedPane.setEnabledAt(1, false);
		}
		settingFrameTitle();
		queryTextField = new JTextField(80);
		applyButton = new JButton("Apply");

		JPanel queryPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Text field and Apply button
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.9;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		queryPanel.add(queryTextField, c);

		c.weightx = 0.1;
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		queryPanel.add(applyButton, c);

		// Boolean Operator Panel
		JPanel booleanPanel = new JPanel(new GridLayout(2, 2));
		addOperatorButtons(booleanPanel, new String[]{"and", "or", "(", ")"}, queryTextField);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		queryPanel.add(createLabeledPanel("Boolean Operator", booleanPanel), c);

		// Relation Operator Panel
		JPanel relationPanel = new JPanel(new GridLayout(2, 3));
		addOperatorButtons(relationPanel, new String[]{"=", "<>", "<", "<=", ">", ">="}, queryTextField);
		c.gridx = 1;
		queryPanel.add(createLabeledPanel("Relation Operator", relationPanel), c);

		// Fields Panel
		JPanel fieldsPanel = new JPanel(new GridLayout(2, 1));
		JComboBox<String> fieldsComboBox = new JComboBox<>(koalaInput.getColumnNames());
		JButton addFieldButton = new JButton("Add field");
		addFieldButton.addActionListener(e -> queryTextField.setText(queryTextField.getText() + " '" +
			fieldsComboBox.getSelectedItem() + "' "));
		fieldsPanel.add(fieldsComboBox);
		fieldsPanel.add(addFieldButton);
		c.gridx = 2;
		queryPanel.add(createLabeledPanel("Fields", fieldsPanel), c);

		// Actions Panel
		JPanel actionsPanel = new JPanel(new GridLayout(2, 1));
		JButton clearSelectionButton = new JButton("Clear Selection");
		JButton confirmButton = new JButton("Confirm");
		clearSelectionButton.addActionListener(e -> queryTextField.setText(""));
		confirmButton.setEnabled(false);
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				koalaInput.setDataSubset(queryTableBuilder.getDataSubset());
				queryFrame.dispose();
			}
		});
		actionsPanel.add(clearSelectionButton);
		actionsPanel.add(confirmButton);
		c.gridx = 3;
		c.gridwidth = 2; // Span across two columns for actions panel
		queryPanel.add(createLabeledPanel("Actions", actionsPanel), c);

		queryFrame.add(tabbedPane, BorderLayout.CENTER);
		queryFrame.add(queryPanel, BorderLayout.SOUTH);

		queryFrame.setSize(800, 600);
		queryFrame.setLocationRelativeTo(null);
		queryFrame.setVisible(true);

		applyButton.addActionListener(e -> {
			sortedJTable = new JTable();
			String queryString = queryTextField.getText();
			queryTableBuilder = new QueryTableBuilder(data, queryString);
			sortedJTable.setModel(queryTableBuilder.getQueryTable());
			sortedRow = sortedJTable.getRowCount();
			sortedCol = sortedJTable.getColumnCount();
			tabbedPane.setComponentAt(1, new JScrollPane(sortedJTable));
			tabbedPane.setEnabledAt(1, true);
			settingFrameTitle();
			confirmButton.setEnabled(true);
		});
	}

	private void addOperatorButtons(JPanel panel, String[] operators, JTextField targetField) {
		for (String operator : operators) {
			JButton button = new JButton(operator);
			button.addActionListener(e -> targetField.setText(targetField.getText() + " " + operator));
			panel.add(button);
		}
	}

	private JPanel createLabeledPanel(String labelText, JPanel panel) {
		JPanel labeledPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(labelText, SwingConstants.CENTER); // label centered
		labeledPanel.add(label, BorderLayout.PAGE_START); //
		labeledPanel.add(panel, BorderLayout.CENTER); //
		return labeledPanel;
	}

	private void settingFrameTitle() {
		queryFrame.setTitle("Original: " + "(" + originalRow + "x" + originalCol + ")" +
			" Sorted: " + "(" + sortedRow + "x" + sortedCol + ")");
	}
}
