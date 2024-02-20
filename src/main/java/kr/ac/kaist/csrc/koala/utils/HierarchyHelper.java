package kr.ac.kaist.csrc.koala.utils;

import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.Data;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

import static kr.ac.kaist.csrc.koala.utils.DataHandle.getRowDefaultTable;
import static kr.ac.kaist.csrc.koala.utils.DataHandle.setHierarchyTableHeader;

public class HierarchyHelper {

	// Hierarchy Data class for Hierarchy Setting
	public static class HierarchyData {

		public Hierarchy hierarchy;
		public String attributeName;
		public String attributeFormat;
		public DefaultTableModel hierarchyDefaultTable;
		public JTable hierarchyJTable;
		public String[] inputValues;
		public AttributeType attrType;

		public HierarchyData(String attributeName, AttributeType attrType) {
			this.attributeName = attributeName;
			this.attrType = attrType;
			this.hierarchyJTable = new JTable();
		}

		public void loadExistOHierarchy(Data data) {
			this.hierarchy = data.getDefinition().getHierarchyObject(attributeName);
			hierarchyToJTable();
		}

		public void hierarchyToJTable() {
			hierarchyDefaultTable = DataHandle.arrayToDefaultTable(this.hierarchy.getHierarchy());
			hierarchyJTable.setModel(hierarchyDefaultTable);
			setHierarchyTableHeader(hierarchyJTable);
		}
	}

	// Hierarchy Data Child Class for IntervalIntervalHierarchy - integerType
	public static class IntegerIntervalHierarchyData extends HierarchyData {

		public long lower, upper, interval;
		public int levNum, levSize;

		public IntegerIntervalHierarchyData(String attributeName, AttributeType attrType, String[] value, long lower,
			long upper, long interval,
			int levNum, int levSize) {
			super(attributeName, attrType);
			this.inputValues = value;
			this.lower = lower;
			this.upper = upper;
			this.interval = interval;
			this.levNum = levNum;
			this.levSize = levSize;
			this.hierarchy = createIntervalHierarchy(this.inputValues, this.lower, this.upper, this.interval,
				this.levNum, this.levSize);
			hierarchyToJTable();
		}
	}

	// Hierarchy Data Child Class for Categorical Hierarchy
	public static class DefaultHierarchyData extends HierarchyData {

		public DefaultHierarchyData(String attributeName, String[] distinct, AttributeType attrType) {
			super(attributeName, attrType);
			this.inputValues = distinct;
			this.attrType = attrType;
			this.hierarchy = createDefaultHierarchy(this.inputValues);
			hierarchyToJTable();
		}
	}

	// Make Sample Data
	public static String[] getSampleData(long lower, long upper) {
		long sampleSize = upper - lower;
		String[] result = new String[(int) sampleSize + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = String.valueOf(i);
		}
		return result;
	}

	// Interval Hierarchy data - Integer Type
	private static Hierarchy createIntervalHierarchy(String[] dataSample, long lower, long upper,
		long interval, int levNum, int levSize) {
		// set double dataType
		DataType<Long> dataType = DataType.INTEGER;

		// Create the builder
		HierarchyBuilderIntervalBased<Long> builder = HierarchyBuilderIntervalBased.create(dataType,
			new HierarchyBuilderIntervalBased.Range<Long>(lower, lower, Long.MIN_VALUE / 4),
			new HierarchyBuilderIntervalBased.Range<Long>(upper, upper, Long.MAX_VALUE / 4));

		// Define base intervals
		builder.setAggregateFunction(dataType.createAggregate().createIntervalFunction(true, true));
		builder.addInterval(lower, lower + interval);

		// Define grouping
		for (int i = 0; i < levNum; i++) {
			builder.getLevel(i).addGroup(levSize);
		}

		return builder.build(dataSample);
	}

	//
	private static Hierarchy createDefaultHierarchy(String[] distinctVal) {
		// Distinct Attribute data sorting
		Arrays.sort(distinctVal);
		// Create the builder
		DefaultHierarchy defaultHierarchy = Hierarchy.create();
		for (String value : distinctVal) {
			defaultHierarchy.add(value, "*");
		}
		return defaultHierarchy;
	}

	public static Hierarchy hierarchyDatatoHierarchy(HierarchyData inputHierarchyData) {
		getRowDefaultTable(inputHierarchyData.hierarchyDefaultTable, 1);
		DefaultHierarchy outputHierarchy = Hierarchy.create();
		for (int i = 0; i < inputHierarchyData.hierarchyDefaultTable.getRowCount(); i++) {
			outputHierarchy.add(getRowDefaultTable(inputHierarchyData.hierarchyDefaultTable, i));
		}
		return outputHierarchy;
	}
}
