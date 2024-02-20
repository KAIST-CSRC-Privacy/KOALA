package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.gui.tableRenderer.SortTableCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

import static kr.ac.kaist.csrc.koala.gui.structure.HierarchyFrame.sensitiveMap;
import static kr.ac.kaist.csrc.koala.gui.structure.SensitiveSetting.sensitiveArray;

public class KoalaFeature {

	DefaultTableModel featureTable;
	JTable featureJTable = new JTable();
	KoalaResult koalaResult;
	String sensitiveType;
	String[] rates;
	String[] sensitiveIndice;
	String[] sensitiveAttributeArray;
	private final String[] features = {"AS", "SK", "SI", "SE", "NR", "PR", "DU"};

	public KoalaFeature(KoalaResult koalaResult, String sensitiveType) {
		this.sensitiveType = sensitiveType;
		this.koalaResult = koalaResult;
		sensitiveAttributeArray = koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(),
			koalaResult.sensitiveAttribute);
		if (sensitiveType.equals("Numerical")) {
			featureTable = buildNumericalFeatureTable(setNumericalFeatureArray(proximitySummary()));
		} else {
			featureTable = buildCategoricalFeatureTable(
				setFeatureArray(summaryRates(), summarySensitivity(), summarySim()));
		}
		featureJTable.setModel(featureTable);
		featureJTable.setDefaultRenderer(Object.class, new SortTableCellRenderer());
	}

	private String[] setFeatureArray(String[] rates, String[] sensitivities, String[] similarities) {
		int length = rates.length;
		String[] featureArray = new String[length];

		for (int i = 0; i < length; i++) {
			StringBuilder featureBuilder = new StringBuilder();
			double rateValue = Double.parseDouble(rates[i]);
			if (sensitivities.length != 0) {
				double senRateValue = Double.parseDouble(sensitivities[i]);
				if (senRateValue > 0.5) {
					featureBuilder.append("SE ");
				}
			}
			if (similarities.length != 0) {
				double simRateValue = Double.parseDouble(similarities[i]);
				if (simRateValue > 0.5) {
					featureBuilder.append("SI ");
				}
			}
			if (rateValue == 1) {
				featureBuilder.append("AS ");
			}
			if (rateValue > 0.5) {
				featureBuilder.append("SK ");
			}
			featureArray[i] = featureBuilder.toString();
		}
		return featureArray;
	}

	private DefaultTableModel buildCategoricalFeatureTable(String[] featureCategoricalArray) {
		DefaultTableModel original = koalaResult.getAnonymizedSummary();
		DefaultTableModel output = cloneTableModel(original);
		output.addColumn("feature", featureCategoricalArray);
		output.addColumn("rates", rates);
		return output;
	}

	private DefaultTableModel cloneTableModel(DefaultTableModel model) {
		DefaultTableModel clone = new DefaultTableModel();
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			clone.addColumn(model.getColumnName(columnIndex));
		}
		for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
			clone.addRow((Vector) model.getDataVector().get(rowIndex));
		}
		return clone;
	}

	private int totalArrayCalculation(ArrayList<String> list) {
		int total = 0;
		for (String value : list) {
			total += Integer.parseInt(value);
		}
		return total;
	}

	private String[] summaryRates() {
		//init
		int rowCount = koalaResult.getAnonymizedSummary().getRowCount();
		String[] rateArray = new String[rowCount];

		//Divide counts by EC
		Map<String, ArrayList<String>> countMap =
			equivalenceClassAlign(koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(), "EC"),
				koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(), "counts"));
		Map<String, Integer> totalMap = new HashMap<>();

		//Initialize totalList
		for (String key : countMap.keySet()) {
			totalMap.put(key, totalArrayCalculation(countMap.get(key)));
		}

		int index = 0;
		int keyNum = countMap.keySet().size();
		for (int i = 1; i < keyNum + 1; i++) {
			String key = String.valueOf(i);
			ArrayList<String> counts = countMap.get(key);
			int total = totalMap.get(key);
			for (String count : counts) {
				double rate = (double) Integer.parseInt(count) / total;
				rateArray[index++] = String.format("%.2f", rate);
			}
		}
		rates = rateArray;
		return rateArray;
	}

	//Sensitivity summary function
	private String[] summarySensitivity() {
		//init
		int rowCount = koalaResult.getAnonymizedSummary().getRowCount();
		String[] sensitivityArray = new String[rowCount];

		//Divide counts by EC
		Map<String, ArrayList<String>> indexMap =
			equivalenceClassAlign(koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(), "EC"),
				sensitivityArrayBuilder(sensitivityArray));
		Map<String, Integer> sizeMap = new HashMap<>();

		//Initialize totalList
		for (String key : indexMap.keySet()) {
			sizeMap.put(key, indexMap.get(key).size());
		}

		//fix this, and it ends
		int index = 0;
		int keyNum = sizeMap.keySet().size();
		for (int i = 1; i < keyNum + 1; i++) {
			String key = String.valueOf(i);

			int sum = arraySum(indexMap.get(key));
			int size = indexMap.get(key).size();

			double rate = (double) sum / size;
			for (int j = 0; j < size; j++) {
				sensitivityArray[index++] = String.format("%.2f", rate);
			}
		}
		sensitiveIndice = sensitivityArray;
		return sensitivityArray;
	}

	//Similarity summary function
	private String[] summarySim() {
		int rowCount = koalaResult.getAnonymizedSummary().getRowCount();
		String[] simArray = new String[rowCount];

		ArrayList<String> result = new ArrayList<>();
		if (sensitiveAttributeArray.length == 0 || sensitiveMap == null) {
			return new String[0];
		}
		for (int i = 0; i < rowCount; i++) {
			simArray[i] = sensitiveMap.get(sensitiveAttributeArray[i]);
		}

		Map<String, ArrayList<String>> simMap =
			equivalenceClassAlign(koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(), "EC"), simArray);
		for (int j = 1; j < simMap.size() + 1; j++) {
			for (int k = 0; k < simMap.get(String.valueOf(j)).size(); k++) {
				result.add(String.valueOf(rateElement(simMap.get(String.valueOf(j)), k)));
			}
		}
		//sensitiveMap;
		return result.toArray(new String[result.size()]);
	}

	private double rateElement(ArrayList<String> input, int index) {
		int size = input.size();
		int count = 0;
        for (String s : input) {
            if (input.get(index).equals(s)) {
                count++;
            }
        }
		return (double) count / size;
	}

	private int arraySum(ArrayList<String> input) {
		int inputSize = input.size();
		int result = 0;
        for (String s : input) {
            result = result + Integer.parseInt(s);
        }
		return result;
	}

	private Map<String, ArrayList<String>> equivalenceClassAlign(String[] key, String[] target) {

		String[] distinctKey = koalaResult.distinctColumnValue(key);

		Map<String, ArrayList<String>> align = new HashMap<>();
		//align initial setting
		for (String i : distinctKey) {
			align.put(i, new ArrayList<>());
		}
		//values
		for (int i = 0; i < key.length; i++) {
			align.get(key[i]).add(target[i]);
		}
		return align;
	}

	private String[] sensitivityArrayBuilder(String[] sensitiveAttribute) {
		ArrayList<String> sensitiveOrInsensitiveIndex = new ArrayList<>();
        for (String s : sensitiveAttribute) {
            if (sensitiveArray.contains(s)) {
                sensitiveOrInsensitiveIndex.add("1");
            } else {
                sensitiveOrInsensitiveIndex.add("0");
            }
        }
		return sensitiveOrInsensitiveIndex.toArray(new String[0]);
	}

	private DefaultTableModel buildNumericalFeatureTable(String[] featureNumericalArray) {
		DefaultTableModel original = koalaResult.getAnonymizedSummary();
		DefaultTableModel output = cloneTableModel(original);
		output.addColumn("feature", featureNumericalArray);
		output.addColumn("proximityRates", proximitySummary());
		return output;
	}

	private String[] proximitySummary() {
		ArrayList<String> result = new ArrayList<>();
		Map<String, ArrayList<String>> proxMap = equivalenceClassAlign(
			koalaResult.getColumnStringArray(koalaResult.getAnonymizedSummary(), "EC"), sensitiveAttributeArray);
		for (int j = 1; j < proxMap.size() + 1; j++) {
			for (int k = 0; k < proxMap.get(String.valueOf(j)).size(); k++) {
				result.add(String.valueOf(proximityCalculation(proxMap.get(String.valueOf(j)), k, 0.3)));
			}
		}
		return result.toArray(new String[0]);
	}

	private String proximityCalculation(ArrayList<String> input, int index, double threshold) {
		int rowCount = input.size();
		int meetCount = 0;
		double minVal = (1 - threshold) * Double.parseDouble(input.get(index));
		double maxVal = (1 + threshold) * Double.parseDouble(input.get(index));
        for (String s : input) {
            if (Double.parseDouble(s) >= minVal && Double.parseDouble(s) <= maxVal) {
                meetCount++;
            }
        }
		return String.valueOf(meetCount / (double) rowCount);
	}

	private String[] setNumericalFeatureArray(String[] proximityValues) {
		int length = proximityValues.length;
		String[] featureArray = new String[length];
		for (int i = 0; i < length; i++) {
			StringBuilder featureBuilder = new StringBuilder();
			if (Double.parseDouble(proximityValues[i]) > 0.5) {
				featureBuilder.append("PX ");
			}
			featureArray[i] = featureBuilder.toString();
		}
		return featureArray;
	}

	public JTable getFeatureJTable() {
		return featureJTable;
	}
}

