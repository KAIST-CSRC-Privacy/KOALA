package kr.ac.kaist.csrc.koala.gui.structure;


import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.criteria.*;
import org.deidentifier.arx.metric.Metric;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;


import static kr.ac.kaist.csrc.koala.ExprData.*;


public class ModelSelectionPanel {

	// Configuration and Input Data
	private ARXConfiguration config;
	private KoalaInput koalaInput;

	// UI Components: Panels and Scroll Panes
	public JPanel modelSelectionPanel;
	private JPanel modelPanel, parameterPanel, attributePanel, modelListPanel, metricPanel, parameterOptionPane;
	private static JScrollPane attributeScroll, modelScroll, privacyModelPane;

	// UI Components: Tables, Models, and Lists
	public static JTable attributeJTable;
	public JTable allPrivacyModelJTable;
	public DefaultTableModel allPrivacyModelTable, attributeTable, featureTable;
	public static DefaultListModel<Object> modelDefaultList;
	private JList<Object> modelJList;

	// UI Components: Labels, Text Fields, ComboBox, and Buttons
	JLabel modelSelectionLabel, modelParameterLabel, modelAttributeLabel, modelListLabel;
	JLabel firstParameterLabel, secondParameterLabel, metricLabel;
	private JTextField firstParameter, secondParameter;
	private static JComboBox metricComboBox;
	private JButton addButton, deleteButton, parameterButton;

	// Data Storage
	private String[] privacyModelArray, metricStr = {"Average equivalence class size", "Discernibility", "Height", "Loss",
			"Non-uniform entropy", "Precision", "Ambiguity", "Normalized non-uniform entropy", "KL-Divergence",
			"Publisher payout (prosecutor)", "Publisher payout (journalist)", "Entropy-based information loss",
			"Classification accuracy"};
	private ArrayList<String> parameterArrayList;

	// Current State Variables
	public static String currentModel, currentType;

	public ModelSelectionPanel(KoalaInput koalaInput) {
		// initialize
		this.koalaInput = koalaInput;
		config = koalaInput.config;

		modelSelectionPanel = new JPanel();
		modelSelectionPanel.setLayout(new GridLayout(1, 5, 15, 0));
		modelSelectionPanel.setPreferredSize(new Dimension(950, 130));

		parameterArrayList = new ArrayList<>();
		modelDefaultList = new DefaultListModel<>();
		modelJList = new JList<>(modelDefaultList);

		firstParameter = new JTextField(5);
		secondParameter = new JTextField(5);

		attributeJTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		attributeTable = new DefaultTableModel();
		parameterOptionPane = new JPanel();
		featureTable = new DefaultTableModel();

		//First Panel (Label and Combo Box)
		modelPanel = new JPanel();
		modelSelectionLabel = new JLabel("1. Model Selection");
		modelSelectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		allPrivacyModelTable = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		allPrivacyModelJTable = new JTable();
		privacyModelArray = new String[]{"k-Anonymity", "l-Diversity", "Entropy l-Diversity",
			"Recursive (c,l)-Diversity", "Ordered t-Closeness", "Equal t-Closeness", "Hierarchical t-Closeness",
			"Differential Privacy", "Beta-likeness", "Delta-presence"};
		allPrivacyModelTable.addColumn("Privacy Model", privacyModelArray);
		allPrivacyModelJTable.setModel(allPrivacyModelTable);
		privacyModelPane = new JScrollPane(allPrivacyModelJTable);

		//BoxLayout Setting
		modelPanel.setLayout(new BoxLayout(modelPanel, BoxLayout.Y_AXIS));
		modelPanel.add(modelSelectionLabel);
		modelPanel.add(privacyModelPane);

		//Second Panel (Label and Text Field)
		parameterPanel = new JPanel();
		modelParameterLabel = new JLabel("2. Model Parameter");
		modelParameterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		parameterButton = new JButton("Press this button...");
		parameterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		//BoxLayout Setting
		parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
		parameterPanel.add(modelParameterLabel);
		parameterPanel.add(parameterButton);

		//Third Panel (Label and JList)
		attributePanel = new JPanel();
		modelAttributeLabel = new JLabel("3. Model Attribute");
		modelAttributeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		attributeScroll = new JScrollPane();
		attributeScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

		addButton = new JButton("Add");
		deleteButton = new JButton("Delete");
		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
		JPanel btnPanel = new JPanel();
		btnPanel.add(addButton);
		btnPanel.add(deleteButton);

		attributePanel.setLayout(new BoxLayout(attributePanel, BoxLayout.Y_AXIS));
		attributePanel.add(modelAttributeLabel);
		attributePanel.add(attributeScroll);
		attributePanel.add(btnPanel);

		//Fourth Panel (JList)
		modelListPanel = new JPanel();
		modelListLabel = new JLabel("4. Model List");
		modelListLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		modelScroll = new JScrollPane(modelJList) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		modelScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

		//BoxLayout Setting
		modelListPanel.setLayout(new BoxLayout(modelListPanel, BoxLayout.Y_AXIS));
		modelListPanel.add(modelListLabel);
		modelListPanel.add(modelScroll);

		//Fifth Panel (Jcombo box)
		metricComboBox = new JComboBox(metricStr);
		metricComboBox.setSelectedIndex(1);
		metricPanel = new JPanel();
		metricLabel = new JLabel("5. Metric Selection");
		metricLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		metricComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		metricPanel.setLayout(new BoxLayout(metricPanel, BoxLayout.Y_AXIS));
		metricPanel.add(metricLabel);
		metricPanel.add(metricComboBox);

		modelSelectionPanel.add(modelPanel);
		modelSelectionPanel.add(parameterPanel);
		modelSelectionPanel.add(attributePanel);
		modelSelectionPanel.add(modelListPanel);
		modelSelectionPanel.add(metricPanel);

		// Model Add Button Setting

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!attributeJTable.isColumnSelected(0)) {
					JOptionPane.showMessageDialog(null, "Please select an attribute");
					return;
				}

				String attributeName = attributeJTable.getValueAt(attributeJTable.getSelectedRow(), 0).toString();
				int selectedRow = allPrivacyModelJTable.getSelectedRow();

				//Anonymization setting
				switch (selectedRow) {
					case 0 -> addKAnonymityModel();
					case 1 -> addDistinctLDiversityModel(attributeName);
					case 2 -> addEntropyLDiversityModel(attributeName);
					case 3 -> addRecursiveCLDiversityModel(attributeName);
					case 4 -> addOrderedDistanceTClosenessModel(attributeName);
					case 5 -> addEqualDistanceTClosenessModel(attributeName);
					case 6 -> addHierarchicalDistanceTClosenessModel(attributeName);
					case 7 -> addEDDifferentialPrivacyModel();
					case 8 -> addEnhancedBLikenessModel(attributeName);
					case 9 -> addDPresenceModel(koalaInput);
					default -> JOptionPane.showMessageDialog(null, "Invalid selection");
				}

				updateModelList();
			}
		});

		// Model Delete Button
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					config.getPrivacyModels()
						.remove(config.getPrivacyModels().toArray()[modelJList.getSelectedIndex()]);
					modelDefaultList.remove(modelJList.getSelectedIndex());

				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Pleases select a model");
				}
			}
		});

		///Need to simplify
		parameterButton.addActionListener(e -> {
            try {
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("k-Anonymity")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "k-value", 2);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("QI", "k-Anonymity");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("l-Diversity")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "l-value", 2);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "l-Diversity");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Entropy l-Diversity")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "l-value", 2);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "Entropy l-Diversity");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Recursive (c,l)-Diversity")) {
                    parameterArrayList.clear();
                    parameterOptionPane.setLayout(new GridLayout(2, 2));
                    firstParameterLabel = new JLabel("c : ");
                    secondParameterLabel = new JLabel("l : ");
                    parameterOptionPane.add(firstParameterLabel);
                    parameterOptionPane.add(firstParameter);
                    parameterOptionPane.add(secondParameterLabel);
                    parameterOptionPane.add(secondParameter);
                    JOptionPane.showMessageDialog(null, parameterOptionPane);
                    parameterArrayList.add(firstParameter.getText());
                    parameterArrayList.add(secondParameter.getText());
                    attributeTableSetting("Sensitive", "Recursive (c,l)-Diversity");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Ordered t-Closeness")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "t-value", 0.1);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "Ordered t-Closeness");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Equal t-Closeness")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "t-value", 0.1);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "Equal t-Closeness");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Hierarchical t-Closeness")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "t-value", 0.1);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "Hierarchical t-Closeness");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Differential Privacy")) {
                    parameterArrayList.clear();
                    parameterOptionPane.setLayout(new GridLayout(2, 2));
                    firstParameterLabel = new JLabel("epsilon : ");
                    secondParameterLabel = new JLabel("delta : ");
                    parameterOptionPane.add(firstParameterLabel);
                    parameterOptionPane.add(firstParameter);
                    parameterOptionPane.add(secondParameterLabel);
                    parameterOptionPane.add(secondParameter);
                    JOptionPane.showMessageDialog(null, parameterOptionPane);
                    parameterArrayList.add(firstParameter.getText());
                    parameterArrayList.add(secondParameter.getText());
                    attributeTableSetting("QI", "Differential Privacy");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Beta-likeness")) {
                    parameterArrayList.clear();
                    String parameter = JOptionPane.showInputDialog(null, "beta-value", 0.1);
                    parameterArrayList.add(parameter);
                    attributeTableSetting("Sensitive", "Beta-likeness");
                }
                if (allPrivacyModelJTable.getValueAt(allPrivacyModelJTable.getSelectedRow(), 0)
                    .equals("Delta-presence")) {
                    parameterArrayList.clear();
                    parameterOptionPane.setLayout(new GridLayout(2, 2));
                    firstParameterLabel = new JLabel("Delta Min : ");
                    secondParameterLabel = new JLabel("Delta Max : ");
                    parameterOptionPane.add(firstParameterLabel);
                    parameterOptionPane.add(firstParameter);
                    parameterOptionPane.add(secondParameterLabel);
                    parameterOptionPane.add(secondParameter);
                    JOptionPane.showMessageDialog(null, parameterOptionPane);
                    parameterArrayList.add(firstParameter.getText());
                    parameterArrayList.add(secondParameter.getText());
                    attributeTableSetting("QI", "Delta-Presence");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Select a Privacy Model");
            }
            addButton.setEnabled(true);
        });
	}

	private void addKAnonymityModel() {
		try {
			config.addPrivacyModel(new KAnonymity(Integer.parseInt(parameterArrayList.get(0))));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "No Duplicate");
		}
	}

	private void addDistinctLDiversityModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("distinct"));
		config.addPrivacyModel(new DistinctLDiversity(attributeName, Integer.parseInt(parameterArrayList.get(0))));
	}

	private void addEntropyLDiversityModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("entropy"));
		config.addPrivacyModel(new EntropyLDiversity(attributeName, Integer.parseInt(parameterArrayList.get(0))));
	}

	private void addRecursiveCLDiversityModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("recursive"));
		config.addPrivacyModel(new RecursiveCLDiversity(attributeName, Double.parseDouble(parameterArrayList.get(0)),
			Integer.parseInt(parameterArrayList.get(1))));
	}

	private void addOrderedDistanceTClosenessModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("closeness"));
		config.addPrivacyModel(
			new OrderedDistanceTCloseness(attributeName, Double.parseDouble(parameterArrayList.get(0))));
	}

	private void addEqualDistanceTClosenessModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("closeness"));
		config.addPrivacyModel(
			new EqualDistanceTCloseness(attributeName, Double.parseDouble(parameterArrayList.get(0))));
	}

	private void addHierarchicalDistanceTClosenessModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels()
			.removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("closeness"));

		config.addPrivacyModel(new HierarchicalDistanceTCloseness(attributeName,
			Double.parseDouble(parameterArrayList.get(0)), structuredData.getDefinition().
			getHierarchyObject(attributeName)));
	}

	private void addEDDifferentialPrivacyModel() {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels().removeIf(p -> p.toString().contains("DP"));
		config.addPrivacyModel(new EDDifferentialPrivacy(Double.parseDouble(parameterArrayList.get(0)),
			Double.parseDouble(parameterArrayList.get(1))));
		config.setDPSearchBudget(0.1);
		config.setHeuristicSearchStepLimit(36);
	}

	private void addEnhancedBLikenessModel(String attributeName) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels().removeIf(p -> p.toString().contains(attributeName) & p.toString().contains("Beta"));
		config.addPrivacyModel(new EnhancedBLikeness(attributeName, Double.parseDouble(parameterArrayList.get(0))));
	}

	private void addDPresenceModel(KoalaInput koalaInput) {
		if (!attributeJTable.isColumnSelected(0)) {
			JOptionPane.showMessageDialog(null, "Please select an attribute");
			return;
		}
		config.getPrivacyModels().removeIf(p -> p.toString().contains("Delta"));
		config.addPrivacyModel(new DPresence(Double.parseDouble(parameterArrayList.get(0)),
			Double.parseDouble(parameterArrayList.get(1)), koalaInput.getDataSubset()));
	}

	private void updateModelList() {
		modelDefaultList.removeAllElements();
		for (int i = 0; i < config.getPrivacyModels().size(); i++) {
			modelDefaultList.addElement(config.getPrivacyModels().toArray()[i]);
		}
		deleteButton.setEnabled(true);
	}

	private void attributeTableSetting(String type, String model) {
		currentModel = model;
		currentType = type;
		attributeTableUpdate();
	}

	public void attributeTableUpdate() {
		if (currentModel == null || currentType == null) {
			return;
		}
		DefaultTableModel attributeTable = new DefaultTableModel();
		if (currentType.equals("QI")) {
			attributeTable.addColumn(currentModel + " Attribute", new String[]{"Quasi-Identifiers"});
		} else {
			attributeTable.addColumn(currentModel + " Attribute", new String[]{koalaInput.sensitiveValue});
		}
		attributeJTable.setModel(attributeTable);
		attributeScroll.setViewportView(attributeJTable);
	}

	//Metric Setting
	public static void metricSetting(ARXConfiguration config) {
		String selectedMetric = Objects.requireNonNull(metricComboBox.getSelectedItem()).toString();

		switch (selectedMetric) {
			case "Average equivalence class size" -> setMetric(config, Metric::createAECSMetric);
			case "Discernibility" -> setMetric(config, Metric::createDiscernabilityMetric);
			case "Height" -> setMetric(config, Metric::createHeightMetric);
			case "Loss" -> setMetric(config, Metric::createLossMetric);
			case "Non-uniform entropy" -> setMetric(config, Metric::createEntropyMetric);
			case "Precision" -> setMetric(config, Metric::createPrecisionMetric);
			case "Ambiguity" -> setMetric(config, Metric::createAmbiguityMetric);
			case "Normalized non-uniform entropy" -> setMetric(config, Metric::createNormalizedEntropyMetric);
			case "KL-Divergence" -> setMetric(config, Metric::createKLDivergenceMetric);
			case "Publisher payout (prosecutor)" -> setMetric(config, () -> Metric.createPublisherPayoutMetric(false));
			case "Publisher payout (journalist)" -> setMetric(config, () -> Metric.createPublisherPayoutMetric(true));
			case "Entropy-based information loss" -> setMetric(config, Metric::createEntropyBasedInformationLossMetric);
			default -> setMetric(config, Metric::createClassificationMetric);
		}
	}

	private static void setMetric(ARXConfiguration config, Supplier<Metric<?>> metricSupplier) {
		config.setQualityModel(metricSupplier.get());
	}
}