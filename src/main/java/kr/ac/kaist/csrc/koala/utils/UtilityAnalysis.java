package kr.ac.kaist.csrc.koala.utils;

import kr.ac.kaist.csrc.koala.gui.structure.KoalaInput;
import kr.ac.kaist.csrc.koala.gui.structure.KoalaResult;
import org.deidentifier.arx.DataHandle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Objects;

public class UtilityAnalysis {

	DataHandle anonymizedHandle;
	String title;
	double allCellNum = 0;
	double allTypeNum = 0;
	double allDiffNum = 0;
	double genIntensity;
	double granularity;
	double nuEntropy;
	double discernibility;
	double averageClassSize;
	double recordLevelSE;
	double attributeLevelSE;
	double minimumDistortion1;
	double minimumDistortion2;

	public UtilityAnalysis(KoalaInput koalaInput, KoalaResult koalaResult, String title) {
		koalaResult.getArxResult().getInput().release();
		this.anonymizedHandle = koalaResult.getArxResult().getOutput();
		this.title = title;
		allCellNum = getAllCellNum(koalaResult);
		allTypeNum = getTypeCellNum(koalaResult, koalaInput);
		allDiffNum = getDiffCellNum(koalaResult);

		genIntensity = anonymizedHandle.getStatistics().getQualityStatistics().getGeneralizationIntensity()
			.getArithmeticMean();
		granularity = anonymizedHandle.getStatistics().getQualityStatistics().getGranularity().getArithmeticMean();
		nuEntropy = anonymizedHandle.getStatistics().getQualityStatistics().getNonUniformEntropy().getArithmeticMean();
		discernibility = anonymizedHandle.getStatistics().getQualityStatistics().getDiscernibility().getValue();
		averageClassSize = anonymizedHandle.getStatistics().getQualityStatistics().getAverageClassSize().getValue();
		recordLevelSE = anonymizedHandle.getStatistics().getQualityStatistics().getRecordLevelSquaredError().getValue();
		attributeLevelSE = anonymizedHandle.getStatistics().getQualityStatistics().getAttributeLevelSquaredError()
			.getArithmeticMean();
		minimumDistortion1 = 1 - (allDiffNum / allCellNum);
		minimumDistortion2 = 1 - (allDiffNum / allTypeNum);
	}

	public JScrollPane attributeAnalysis() {
		DefaultTableModel model = new DefaultTableModel(
			new Object[]{"Attribute", "Data Type", "Missings", "Gen. intensity", "Granularity", "N.U. entropy",
				"Squared error"}, 0);

		String att;
		String type;
		double miss;
		double genIn;
		double gran;
		double nuE;
		double sqE;
		for (String i : anonymizedHandle.getDefinition().getQuasiIdentifyingAttributes()) {
			att = i;
			type = String.valueOf(anonymizedHandle.getDefinition().getAttributeType(i));
			miss = anonymizedHandle.getStatistics().getQualityStatistics().getMissings().getValue(i);
			genIn = anonymizedHandle.getStatistics().getQualityStatistics().getGeneralizationIntensity().getValue(i);
			gran = anonymizedHandle.getStatistics().getQualityStatistics().getGranularity().getValue(i);
			nuE = anonymizedHandle.getStatistics().getQualityStatistics().getNonUniformEntropy().getValue(i);
			sqE = anonymizedHandle.getStatistics().getQualityStatistics().getAttributeLevelSquaredError().getValue(i);

			model.addRow(new Object[]{att, type, miss, genIn, gran, nuE, sqE});
		}

		JTable table = new JTable(model) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),
			"Anonymized Attribute Quality",
			TitledBorder.CENTER,
			TitledBorder.TOP));

		//not apply to the attribute and data type column
		for (int i = 2; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(new PercentageBarRenderer());
		}

		return scrollPane;
	}

	public JScrollPane utilityJTable() {
		DefaultTableModel model = new DefaultTableModel(new Object[]{"Model", "Quality"}, 0);
		model.addRow(new Object[]{"Discernibility Metric (DM)", discernibility});
		model.addRow(new Object[]{"Minimum Distortion by all attributes (MD-All)", minimumDistortion1});
		model.addRow(
			new Object[]{"Minimum Distortion by Quasi and Sensitive Attributes (MD-Partial)", minimumDistortion2});
		model.addRow(new Object[]{"Gen. Intensity", genIntensity});
		model.addRow(new Object[]{"Granularity", granularity});
		model.addRow(new Object[]{"N.U. Entropy", nuEntropy});
		model.addRow(new Object[]{"Average Class Size", averageClassSize});
		model.addRow(new Object[]{"Record Level Squared Error", recordLevelSE});
		model.addRow(new Object[]{"Attribute Level Squared Error", attributeLevelSE});

		JTable table = new JTable(model) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JScrollPane scrollPane = new JScrollPane(table);

		scrollPane.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(),
			title,
			TitledBorder.CENTER,
			TitledBorder.TOP));

		table.getColumnModel().getColumn(1).setCellRenderer(new PercentageBarRenderer());

		return scrollPane;
	}

	static class PercentageBarRenderer extends JProgressBar implements TableCellRenderer {

		public PercentageBarRenderer() {
			super(0, 100);
			setValue(0);
			setStringPainted(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
			if (value instanceof Double) {
				int intValue = (int) (100 * (Double) value);
				setValue(intValue);
			}
			return this;
		}
	}

	private int getDiffCellNum(KoalaResult koalaResult) {
		DefaultTableModel original = koalaResult.getOriginalTableSorted();
		DefaultTableModel anonymized = koalaResult.getAnonymizedTableSorted();
		int rowCounts = original.getRowCount();
		int columnCounts = original.getColumnCount();
		int differentCellsCount = 0;
		for (int i = 0; i < rowCounts; i++) {
			for (int j = 0; j < columnCounts; j++) {
				Object value1 = original.getValueAt(i, j);
				Object value2 = anonymized.getValueAt(i, j);
				if ((value1 == null && value2 != null) || (value1 != null && !value1.equals(value2))) {
					differentCellsCount++;
				}
			}
		}
		return differentCellsCount;
	}

	private int getAllCellNum(KoalaResult koalaResult) {
		int row = koalaResult.getAnonymizedTableSorted().getRowCount();
		int col = koalaResult.getAnonymizedTableSorted().getColumnCount();
		return row * col;
	}

	private int getTypeCellNum(KoalaResult koalaResult, KoalaInput koalaInput) {
		int row = koalaResult.getAnonymizedTableSorted().getRowCount();
		int col = koalaInput.currentQID.size();
		if (!Objects.isNull(koalaResult.sensitiveAttribute)) {
			col += 1;
		}
		return row * col;
	}
}
