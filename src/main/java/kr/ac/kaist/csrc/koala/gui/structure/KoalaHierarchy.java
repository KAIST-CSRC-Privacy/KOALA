package kr.ac.kaist.csrc.koala.gui.structure;

import org.deidentifier.arx.AttributeType;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class KoalaHierarchy {

	DefaultTableModel hierarchyAttributeTable;
	DefaultTableModel hierarchyTreeTable;
	private KoalaInput koalaInput;

	public KoalaHierarchy(KoalaInput koalaInput) {
		this.koalaInput = koalaInput;
		updateHierarchyAttributeTable(koalaInput);
		updateHierarchyTreeTable(koalaInput);
	}

	private DefaultTableModel setHierarchyAttributeTable(KoalaInput koalaInput) {
		return (koalaInput.attributeArrayInit());
	}

	private DefaultTableModel setHierarchyTreeTable(KoalaInput koalaInput) {
		DefaultTableModel output = new DefaultTableModel(new String[]{"Attribute", "Domain Size", "Tree Level"}, 0);
		ArrayList<String[]> temp = new ArrayList<>();
		sortAttributeTree(temp, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE.toString());
		sortAttributeTree(temp, AttributeType.SENSITIVE_ATTRIBUTE.toString());

		for (String[] i : temp) {
			output.addRow(i);
		}
		return output;
	}

	private void sortAttributeTree(ArrayList<String[]> input, String attributeType) {
		for (int i = 0; i < koalaInput.getLoadedTable().getColumnCount(); i++) {
			String[] temp = new String[3];
			temp[0] = koalaInput.getLoadedData().getHandle().getAttributeName(i);
			if (!koalaInput.getLoadedData().getDefinition().isHierarchyAvailable(temp[0])) {
				continue;
			}
			temp[1] = String.valueOf(koalaInput.getLoadedData().getDefinition().getHierarchy(temp[0]).length);
			temp[2] = String.valueOf(koalaInput.getLoadedData().getDefinition().getHierarchy(temp[0])[0].length);
			if (koalaInput.getLoadedData().getDefinition().getAttributeType(temp[0]).toString().equals(attributeType)) {
				input.add(temp);
			}
		}
	}

	public void updateHierarchyAttributeTable(KoalaInput koalaInput) {
		hierarchyAttributeTable = setHierarchyAttributeTable(koalaInput);
	}

	public void updateHierarchyTreeTable(KoalaInput koalaInput) {
		hierarchyTreeTable = setHierarchyTreeTable(koalaInput);
	}
}
