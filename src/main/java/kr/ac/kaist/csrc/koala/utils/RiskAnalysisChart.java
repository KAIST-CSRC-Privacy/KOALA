package kr.ac.kaist.csrc.koala.utils;

import kr.ac.kaist.csrc.koala.gui.structure.KoalaInput;
import kr.ac.kaist.csrc.koala.gui.structure.KoalaResult;
import org.deidentifier.arx.DataHandle;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


public class RiskAnalysisChart {

	private static final double[] lowerManual = {0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6,
		0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95};
	private static final double[] upperManual = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65,
		0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1};
	private static final int DIAL_CHART_WIDTH = 350;
	private static final int DIAL_CHART_HEIGHT = 250;
	private static final int CATEGORY_CHART_WIDTH = 800;
	private static final int CATEGORY_CHART_HEIGHT = 400;
	private DataHandle resultHandle;
	private DataHandle inputHandle;
	private double threshold;
	private JButton riskRecord = new JButton("Risk Analysis on the records");
	private KoalaInput koalaInput;
	private KoalaResult koalaResult;
	public JPanel riskDialPanel = new JPanel();
	public JPanel riskGraphPanel = new JPanel();
	public JPanel riskTablePanel = new JPanel();
	public JPanel riskRowPanel = new JPanel();
	public JTabbedPane riskPane = new JTabbedPane();

	public RiskAnalysisChart(KoalaInput koalaInput, KoalaResult koalaResult, double threshold) {
		this.koalaResult = koalaResult;
		this.koalaInput = koalaInput;
		koalaResult.getArxResult().getInput().release();
		this.resultHandle = koalaResult.getArxResult().getOutput();
		this.inputHandle = koalaInput.getLoadedData().getHandle();
		this.threshold = threshold;
		riskPaneBuilder();
	}


	private CategoryChart createCategoryChart(String targetRisk, DataHandle handle) {
		List<String> lowerUpperStringArray = lowerUpperBuilder(lowerManual, upperManual);
		List<Double> lowerUpperRiskArray = lowerUpperRiskBuilder(lowerManual, upperManual, handle);

		CategoryChart charts = new CategoryChartBuilder().width(CATEGORY_CHART_WIDTH).height(CATEGORY_CHART_HEIGHT)
			.title(targetRisk + "Risk Distribution").xAxisTitle("Risk")
			.yAxisTitle("Frequency").build();
		//charts.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
		charts.getStyler().setLegendVisible(false);
		charts.getStyler().setToolTipsEnabled(true);
		charts.getStyler().setXAxisLabelRotation(45);
		charts.getStyler().setXAxisMaxLabelCount(12);
		charts.addSeries("Risk Interval", lowerUpperStringArray, lowerUpperRiskArray);
		return charts;
	}

	private DialChart createDialChart(String title, double value) {
		DialChart chart = new DialChartBuilder().width(DIAL_CHART_WIDTH).height(DIAL_CHART_HEIGHT).title(title).build();
		Font legendFont = new Font(Font.SANS_SERIF, Font.PLAIN, 70);
		chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setToolTipsEnabled(true);
		chart.getStyler().setLegendSeriesLineLength(30);
		chart.getStyler().setDecimalPattern("0.####%");
		chart.addSeries(" ", value);

		return chart;
	}

	private JPanel dialChartBuilder(String targetRisk, DataHandle handle, boolean isAnonymizedData) {
		JPanel output = new JPanel();
		output.setLayout(new GridLayout(4, 1));
		output.add(new XChartPanel<DialChart>(createDialChart(targetRisk + "Average Re-identification Risk",
			handle.getRiskEstimator().getSampleBasedReidentificationRisk().getAverageRisk())));
		output.add(new XChartPanel<DialChart>(createDialChart(targetRisk + "Lowest Re-identification Risk",
			handle.getRiskEstimator().getSampleBasedReidentificationRisk().getLowestRisk())));
		output.add(new XChartPanel<DialChart>(createDialChart(targetRisk + "Highest Re-identification Risk",
			handle.getRiskEstimator().getSampleBasedReidentificationRisk().getHighestRisk())));

		if (isAnonymizedData) {
			JPanel riskButtonPanel = new JPanel();
			riskButtonPanel.setLayout(new GridLayout(2, 1));
			JButton findMinButton = new JButton("Show the Lowest Risk");
			JButton findMaxButton = new JButton("Show the Highest Risk");
			riskButtonPanel.add(findMinButton);
			riskButtonPanel.add(findMaxButton);
			output.add(riskButtonPanel);

			findMinButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JScrollPane tempPane = (JScrollPane) riskPane.getComponentAt(2);
					JTable tempJTable = (JTable) tempPane.getViewport().getView();
					int mixRowIndex = findMinRiskRow(tempJTable);

					riskPane.setSelectedIndex(2);
					Rectangle cellRect = tempJTable.getCellRect(mixRowIndex, 0, true);

					JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class,
						tempJTable);
					int scrollPaneHeight = scrollPane.getViewport().getHeight();
					tempJTable.scrollRectToVisible(
						new Rectangle(cellRect.x, cellRect.y - scrollPaneHeight / 2 + cellRect.height / 2,
							cellRect.width, scrollPaneHeight));
				}
			});

			findMaxButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JScrollPane tempPane = (JScrollPane) riskPane.getComponentAt(2);
					JTable tempJTable = (JTable) tempPane.getViewport().getView();
					int maxRowIndex = findMaxRiskRow(tempJTable);

					riskPane.setSelectedIndex(2);
					Rectangle cellRect = tempJTable.getCellRect(maxRowIndex, 0, true);

					JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class,
						tempJTable);
					int scrollPaneHeight = scrollPane.getViewport().getHeight();
					tempJTable.scrollRectToVisible(
						new Rectangle(cellRect.x, cellRect.y - scrollPaneHeight / 2 + cellRect.height / 2,
							cellRect.width, scrollPaneHeight));
				}
			});
		} else {
			JPanel emptyPanel = new JPanel();
			output.add(emptyPanel);
		}

		return output;
	}

	private List<String> lowerUpperBuilder(double[] lower, double[] upper) {
		String[] tempStringArray = new String[lower.length];
		for (int i = 0; i < lower.length; i++) {
			//tempStringArray[i] = "]"+lower[i]+", "+upper[i] +"]";
			tempStringArray[i] = upper[i] * 100 + "%";
		}
		List<String> output = Arrays.asList(tempStringArray);
		return output;
	}

	private List<Double> lowerUpperRiskBuilder(double[] lower, double[] upper, DataHandle handle) {
		double[] tempRiskArray = new double[lower.length];
		for (int i = 0; i < lower.length; i++) {
			tempRiskArray[i] =
				handle.getRiskEstimator().getSampleBasedRiskDistribution()
					.getFractionOfRecordsAtCumulativeRisk(upper[i]) -
					handle.getRiskEstimator().getSampleBasedRiskDistribution()
						.getFractionOfRecordsAtCumulativeRisk(lower[i]);
		}
		List<Double> output = DoubleStream.of(tempRiskArray).boxed().collect(Collectors.toList());
		return output;
	}

	private JPanel addCategoryChartsToOutputPanel(String targetRisk, DataHandle handle) {
		JPanel output = new JPanel();
		output.add(new XChartPanel<>(createCategoryChart(targetRisk, handle)));
		return output;
	}

	private void riskPaneBuilder() {
		riskPane.addTab("Risk Dials", riskDialPanel);
		riskPane.addTab("Risk Graphs", riskGraphPanel);
		riskPane.addTab("Risk Table", riskTablePanel);
		riskPane.addTab("Risk Rows", riskRowPanel);

		riskDialPanelBuilder();
		riskCategoryChartBuilder();

		riskPane.setComponentAt(0, riskDialPanel);
		riskPane.setComponentAt(1, riskGraphPanel);
		riskPane.setComponentAt(2, new JScrollPane(koalaResult.getRiskJTable()));
		riskPane.setComponentAt(3, new JScrollPane(koalaResult.getRiskSelectedJTable()));
	}

	public void riskDialPanelBuilder() {
		riskDialPanel.removeAll();
		riskDialPanel.setLayout(new GridLayout(1, 2));
		riskDialPanel.add(dialChartBuilder("Original Data: ", inputHandle, false));
		riskDialPanel.add(dialChartBuilder("Anonymized Data: ", resultHandle, true));
	}

	public void riskCategoryChartBuilder() {
		riskGraphPanel.removeAll();
		riskGraphPanel.setLayout(new GridLayout(2, 1));
		riskGraphPanel.add(addCategoryChartsToOutputPanel("Original Data: ", inputHandle));
		riskGraphPanel.add(addCategoryChartsToOutputPanel("Anonymized Data: ", resultHandle));

	}

	public int findMaxRiskRow(JTable input) {
		int maxRiskIndex = 0;
		double maxRisk = 0;

		int rowCount = input.getRowCount();
		int columnCount = input.getColumnCount();

		for (int i = 0; i < rowCount; i++) {
			double riskValue = Double.parseDouble((String) koalaResult.getRiskJTable().getValueAt(i, columnCount - 2));
			if (riskValue >= maxRisk) {
				maxRisk = riskValue;
				maxRiskIndex = i;
			}
		}
		return maxRiskIndex;
	}

	public int findMinRiskRow(JTable input) {
		int minRiskIndex = 0;
		double minRisk = 1;

		int rowCount = input.getRowCount();
		int columnCount = input.getColumnCount();

		for (int i = 0; i < rowCount; i++) {
			double riskValue = Double.parseDouble((String) koalaResult.getRiskJTable().getValueAt(i, columnCount - 2));
			if (riskValue <= minRisk) {
				minRisk = riskValue;
				minRiskIndex = i;
			}
		}
		return minRiskIndex;
	}

	public JTabbedPane getRiskPane() {
		return riskPane;
	}
}


