package kr.ac.kaist.csrc.koala;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.Data;

import javax.swing.*;
import java.util.ArrayList;

import static kr.ac.kaist.csrc.koala.gui.structure.ModelSelectionPanel.attributeJTable;
import static kr.ac.kaist.csrc.koala.gui.structure.ModelSelectionPanel.modelDefaultList;

public class ExprData {

	public static Data structuredData;

	public static ArrayList<String> qidArrayList = new ArrayList<>();
	public static DefaultListModel<String> qidListModel = new DefaultListModel<>();
	public static DefaultListModel<String> idListModel = new DefaultListModel<>();
	public static DefaultListModel<String> saListModel = new DefaultListModel<>();
	public static DefaultListModel<String> nonSaListModel = new DefaultListModel<>();
	public static JList<String> JQidList = new JList<>(qidListModel);
	public static JList<String> JIdList = new JList<>(idListModel);
	public static JList<String> JSaListSecond = new JList<>(saListModel);
	public static JList<String> JSaList = new JList<>(saListModel);
	public static JList<String> JNonSaList = new JList<>(nonSaListModel);
	public static DefaultListModel sensitiveValueList = new DefaultListModel<>();
	public static DefaultListModel sensitivityList = new DefaultListModel<>();
	public static ARXConfiguration config = ARXConfiguration.create();
	public static ARXConfiguration featureConfig = ARXConfiguration.create();
	public static int idxSAColumn = -1;

	public static void initData() {
		qidListModel.clear();
		idListModel.clear();
		saListModel.clear();
		nonSaListModel.clear();
		JQidList = new JList<>(qidListModel);
		JIdList = new JList<>(idListModel);
		JSaListSecond = new JList<>(saListModel);
		JSaList = new JList<>(saListModel);
		JNonSaList = new JList<>(nonSaListModel);
		config = ARXConfiguration.create();
		featureConfig = ARXConfiguration.create();
		idxSAColumn = -1;
		attributeJTable.setModel(new JTable().getModel());
		modelDefaultList.removeAllElements();
	}
}