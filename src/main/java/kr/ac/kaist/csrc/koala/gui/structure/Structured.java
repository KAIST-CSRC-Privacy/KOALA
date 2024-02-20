package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.gui.listener.RefreshEventListener;
import kr.ac.kaist.csrc.koala.gui.listener.SaveToCSVListener;
import kr.ac.kaist.csrc.koala.utils.db.KoalaDAO;
import kr.ac.kaist.csrc.koala.utils.RiskAnalysisChart;
import kr.ac.kaist.csrc.koala.utils.UtilityAnalysis;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXResult;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;

import static kr.ac.kaist.csrc.koala.gui.structure.ModelSelectionPanel.metricSetting;
import static kr.ac.kaist.csrc.koala.utils.DataHandle.*;

public class Structured {
	// Class members for data processing and UI components
	KoalaResult koalaResult;
	KoalaFeature koalaFeature;
	public JTabbedPane centerPane, featurePane, riskPane;
	public JPanel eastPanel, originalTablePanel = new JPanel(), modelSettingPanel = new JPanel(),
			anonymizedTablePanel = new JPanel(), compareTablePanel = new JPanel(),
			summaryPanel = new JPanel(), featurePanel = new JPanel(), utilityPanel = new JPanel();
	public JButton eastQueryButton = new JButton("Select from Query");
	public JPanel southPanel = new JPanel();
	public static JTextField fileNameText;
	public static JLabel matrixLabel;
	public FeatureTextArea featureTextArea;
	public JMenuBar menuBar;
	public KoalaInput koalaInput;
	public String dbTableName;
	public KoalaHierarchy koalaHierarchy;
	public ARXResult arxResult;
	public JButton anonymizationButton = new JButton("Anonymization");
	private JMenuItem fileCSVLoad, saveFileAS, saveFileToDB;
	private final RefreshEventListener listener;

	public Structured(RefreshEventListener listener) {
		initCenterPane();
		initEastPanel();
		initSouthPanel();
		menuBar = new JMenuBar();
		enableCondition("preInputFile");
		this.listener = listener;

		anonymizationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				anonymizationProcess(koalaInput);
			}
		});
	}

	public Structured(RefreshEventListener listener, String dbTableName) {
		this.dbTableName = dbTableName;
		koalaInput = new KoalaInput(dbTableName);
		koalaHierarchy = new KoalaHierarchy(koalaInput);

		initCenterPane();
		initEastPanel();
		initSouthPanel();
		menuBar = new JMenuBar();
		this.listener = listener;

		//centerPane update
		modelSettingPanelBuilder(modelSettingPanel, koalaInput);
		centerPane.setComponentAt(0, new JScrollPane(koalaInput.getLoadedJTable()));

		//southPanel update
		fileNameText.setText(dbTableName);
		matrixLabel.setText(
			"     행 :  " + koalaInput.getLoadedTable().getRowCount() + "       열: " + koalaInput.getLoadedTable()
				.getColumnCount());

		enableCondition("afterInputFile");

		anonymizationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				anonymizationProcess(koalaInput);
			}
		});
	}

	private void initCenterPane() {
		centerPane = new JTabbedPane();
		riskPane = new JTabbedPane();
		featurePane = new JTabbedPane();
		featureTextArea = new FeatureTextArea();
		centerPane.addTab("Original Table", originalTablePanel);
		centerPane.addTab("Model Setting", modelSettingPanel);
		centerPane.addTab("Anonymized Table", anonymizedTablePanel);
		centerPane.addTab("Compare Tables", compareTablePanel);
		centerPane.addTab("Anonymized Summary Table", summaryPanel);
		centerPane.addTab("Feature Analysis", featurePane);
		featurePane.addTab("Feature Intro", featureTextArea.getInstruction());
		featurePane.addTab("Feature Analysis", featurePanel);
		centerPane.addTab("Risk Analysis", riskPane);
		centerPane.addTab("Utility Analysis", utilityPanel);
	}

	public void initEastPanel() {
		eastPanel = new JPanel();
		eastPanel.setPreferredSize(new Dimension(250, 350));
		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(eastQueryButton, BorderLayout.SOUTH);
		eastQueryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new QueryFrame(koalaInput);
			}
		});
		eastPanel.repaint();
		eastPanel.revalidate();
	}

	public void initSouthPanel() {
		southPanel = new JPanel();

		JLabel fileNameLabel = new JLabel("File Name : ");
		fileNameText = new JTextField();
		fileNameText.setEnabled(false);
		fileNameText.setPreferredSize(new Dimension(350, 20));
		matrixLabel = new JLabel();

		southPanel.add(fileNameLabel);
		southPanel.add(fileNameText);
		southPanel.add(matrixLabel);

	}

	public void initLoadFileMenuBar() {
		JMenu fileMenu = new JMenu("File");
		fileCSVLoad = new JMenuItem("Load CSV");
		saveFileAS = new JMenuItem("Save as CSV");
		saveFileToDB = new JMenuItem("Save as Koala DB");

		// Initially disable saveAS and saveDB menu items
		saveFileAS.setEnabled(false);
		saveFileToDB.setEnabled(false);

		// Load File and Table
		fileCSVLoad.addActionListener(this::fileCSVLoadAction);

		// Add items to the menu bar and file menu
		menuBar.add(fileMenu);
		fileMenu.add(fileCSVLoad);
		fileMenu.add(saveFileAS);
		fileMenu.add(saveFileToDB);
	}

	// Example method or action listener where you enable the menu items
	public void onFileLoadSuccess() {
		saveFileAS.setEnabled(true);
		saveFileToDB.setEnabled(true);
		saveFileAS.addActionListener(new SaveToCSVListener(koalaInput.getJTableStringArray()));
		saveFileToDB.addActionListener(this::newDBTableSaveAction);
	}

	public void initDBMenuBar() {
		JMenu dbMenu = new JMenu("Database");
		JMenuItem saveDB = new JMenuItem("Save");
		JMenuItem saveNewDB = new JMenuItem("Save As");
		JMenuItem removeDB = new JMenuItem("Remove Current DB Table");

		saveDB.addActionListener(this::dbTableSaveAction);
		saveNewDB.addActionListener(this::newDBTableSaveAction);
		removeDB.addActionListener(this::dbTableRemoveAction);

		menuBar.add(dbMenu);
		dbMenu.add(saveDB);
		dbMenu.add(saveNewDB);
		dbMenu.add(removeDB);
	}

	private void modelSettingPanelBuilder(JPanel input, KoalaInput koalaInput) {
		input.removeAll();
		DataAttributePanel firstPanel = new DataAttributePanel(koalaInput);
		ModelSelectionPanel secondPanel = new ModelSelectionPanel(koalaInput);
		HierarchyPanel thirdPanel = new HierarchyPanel(koalaInput, anonymizationButton);
		input.setLayout(new GridLayout(4, 1));
		input.add(firstPanel.resultPanel);
		input.add(secondPanel.modelSelectionPanel);
		input.add(thirdPanel.taxonomyTreePanel);
	}

	private void compareTablePanelSetting(KoalaResult inputKResult) {
		compareTablePanel.removeAll();
		compareTablePanel.setLayout(new GridLayout(2, 1));
		JScrollPane compareOriginalTablePane = new JScrollPane(inputKResult.getOriginalJTableSorted());
		JScrollPane compareAnonymizedTablePane = new JScrollPane(inputKResult.getAnonymizedJTableSorted());
		compareTablePanel.add(compareOriginalTablePane);
		compareTablePanel.add(compareAnonymizedTablePane);
		compareOriginalTablePane.setPreferredSize(new Dimension(300, 390));
		compareAnonymizedTablePane.setPreferredSize(new Dimension(300, 390));

		BoundedRangeModel model = compareOriginalTablePane.getVerticalScrollBar().getModel();
		compareAnonymizedTablePane.getVerticalScrollBar().setModel(model);
	}

	private void utilityAnalysisSetting(KoalaInput koalaInput, KoalaResult koalaResult) {
		UtilityAnalysis anonymizedUtility = new UtilityAnalysis(koalaInput, koalaResult, "Anonymized Data Quality");
		utilityPanel.removeAll();
		utilityPanel.setLayout(new GridLayout(1, 2));
		utilityPanel.add(anonymizedUtility.utilityJTable());
		utilityPanel.add(anonymizedUtility.attributeAnalysis());
	}

	public void dbTableRemoveAction(ActionEvent e) {
		int confirmed = JOptionPane.showConfirmDialog(null,
			"Are you sure you want to drop the table: " + dbTableName + "?", "Confirm Table Drop",
			JOptionPane.YES_NO_OPTION);

		if (confirmed == JOptionPane.YES_OPTION) {
			try (Connection conn = KoalaDAO.makeDBConnect()) {
				KoalaDAO.dropTable(conn, dbTableName);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
					"Failed to drop the table: " + dbTableName + ".\n" + ex.getMessage(), "DB Remove Error",
					JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Table drop cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
		}
		listener.onRefresh();
	}

	public void dbTableSaveAction(ActionEvent e) {
		String[][] loadedTable = koalaInput.getJTableStringArray();
		try {
			Connection conn = KoalaDAO.makeDBConnect();
			KoalaDAO.updateDBTable(conn, dbTableName, loadedTable);
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "DB Save Error", "Error", JOptionPane.ERROR_MESSAGE);
		}
		listener.onRefresh();
	}

	public void newDBTableSaveAction(ActionEvent e) {
		String tableName = null;
		tableName = JOptionPane.showInputDialog(null, "Enter the new DB table name (max 20 characters):",
			"Input Table Name", JOptionPane.QUESTION_MESSAGE);
		if (tableName == null) {
			return;
		} else if (tableName.length() <= 20) {
			String[][] loadedTable = koalaInput.getJTableStringArray();
			try {
				Connection conn = KoalaDAO.makeDBConnect();
				KoalaDAO.saveNewDBTable(conn, tableName, loadedTable);
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "DB Save Error", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "The table name must be 20 characters or less.", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
		listener.onRefresh();
	}

	//ActionListener Setting for fileCSVLoadAction
	private void fileCSVLoadAction(ActionEvent e) {
		dbTableName = null;
		JFileChooser fileChooser = new JFileChooser("./Data/");
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File loadedFile = fileChooser.getSelectedFile();
			koalaInput = new KoalaInput(loadedFile);
			koalaHierarchy = new KoalaHierarchy(koalaInput);

			//centerPane update
			modelSettingPanelBuilder(modelSettingPanel, koalaInput);
			centerPane.setComponentAt(0, new JScrollPane(koalaInput.getLoadedJTable()));

			//southPanel update
			fileNameText.setText(koalaInput.getLoadedFile().getName());
			matrixLabel.setText("     행 :  " + koalaInput.getLoadedTable().getRowCount() + "       " +
				"열: " + koalaInput.getLoadedTable().getColumnCount());

			enableCondition("afterInputFile");
			onFileLoadSuccess();
		}
	}

	private void anonymizationProcess(KoalaInput koalaInput) {
		ARXAnonymizer anonymizer = new ARXAnonymizer();

		// metric, suppression level setting
		koalaInput.config.setSuppressionLimit(0d);
		metricSetting(koalaInput.config);
		try {
			arxResult = anonymizer.anonymize(koalaInput.getLoadedData(), koalaInput.config);
			koalaResult = new KoalaResult(koalaInput, arxResult);

			koalaFeature = new KoalaFeature(koalaResult, koalaInput.sensitiveType);
			RiskAnalysisChart riskAnalysisChart = new RiskAnalysisChart(koalaInput, koalaResult, 0.01);
			lastQid = koalaResult.getLastQidAttribute();
			centerPane.setComponentAt(2, new JScrollPane(koalaResult.getAnonymizedJTableSorted()));
			compareTablePanelSetting(koalaResult);
			centerPane.setComponentAt(4, new JScrollPane(koalaResult.getAnonymizedSummaryJTable()));
			featurePane.setComponentAt(0, featureTextArea.getInstruction());
			featurePane.setComponentAt(1, new JScrollPane(koalaFeature.getFeatureJTable()));
			arxResult.getInput().release();
			centerPane.setComponentAt(6, riskAnalysisChart.getRiskPane());

			utilityAnalysisSetting(koalaInput, koalaResult);
			enableCondition("afterAnonymization");
			JOptionPane.showMessageDialog(null, "Anonymization Success");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Anonymization Failed");
			throw new RuntimeException(ex);
		}
	}

	private void enableCondition(String condition) {
		switch (condition) {
			case "preInputFile" -> enableSetting(false, false, false, false, false, false, false, false);
			case "afterInputFile" -> enableSetting(true, true, false, false, false, false, false, false);
			case "afterAnonymization" -> enableSetting(true, true, true, true, true, true, true, true);
			default -> throw new RuntimeException("No Option");
		}
	}

	private void enableSetting(boolean zeroTab, boolean oneTab, boolean twoTab, boolean threeTab, boolean fourTab,
		boolean fiveTab, boolean sixTab, boolean sevenTab) {
		centerPane.setEnabledAt(0, zeroTab);
		centerPane.setEnabledAt(1, oneTab);
		centerPane.setEnabledAt(2, twoTab);
		centerPane.setEnabledAt(3, threeTab);
		centerPane.setEnabledAt(4, fourTab);
		centerPane.setEnabledAt(5, fiveTab);
		centerPane.setEnabledAt(6, sixTab);
		centerPane.setEnabledAt(7, sevenTab);
	}
}
