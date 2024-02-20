package kr.ac.kaist.csrc.koala.gui.structure;

import kr.ac.kaist.csrc.koala.utils.db.KoalaDAO;
import kr.ac.kaist.csrc.koala.gui.listener.NodeClickListener;
import kr.ac.kaist.csrc.koala.gui.listener.RefreshEventListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.sql.Connection;
import java.util.Vector;

public class WestPanel extends JPanel implements RefreshEventListener {

	private JPanel westPanelTop, westPanelBottom;
	private JTree jtree;

	// Tree nodes for different categories
	DefaultMutableTreeNode root, treeStructure, treeUnstructure, treeDB, treeFile;
	DefaultMutableTreeNode treeUnstructureText, treeUnstructureImage, treeUnstructureImageEnc;
	DefaultMutableTreeNode treeUnstructureAudio, treeUnstructureVideo;

	// Listener for node click events
	private NodeClickListener nodeClickListener;

	public WestPanel() {
		setLayout(new BorderLayout());
		initWestPanelTop();
		initWestPanelBottom();

		// Add top and bottom panels to the WestPanel
		add(westPanelTop, BorderLayout.NORTH);
		add(westPanelBottom, BorderLayout.SOUTH);
	}

	public void setNodeClickListener(NodeClickListener listener) {
		this.nodeClickListener = listener;
	}

	// Initializes the top panel with a mode selection and tree view
	private void initWestPanelTop() {
		westPanelTop = new JPanel(new BorderLayout());

		// Initialize Mode Panel with Structure and Unstructure radio buttons
		JPanel modePanel = new JPanel(new FlowLayout());
		JRadioButton stRBtn = new JRadioButton("Structure");
		stRBtn.setSelected(true);
		JRadioButton unRBtn = new JRadioButton("Unstructure");

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(stRBtn);
		btnGroup.add(unRBtn);

		modePanel.add(stRBtn);
		modePanel.add(unRBtn);
		westPanelTop.add(modePanel, BorderLayout.NORTH);

		// Initialize JTree with data
		jtree = new JTree(initDataTree());
		jtree.addTreeSelectionListener(createTreeSelectionListener());
		westPanelTop.add(new JScrollPane(jtree), BorderLayout.CENTER);

		// Add refresh button to update the tree
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> onRefresh());
		westPanelTop.add(refreshButton, BorderLayout.SOUTH);
	}

	private TreeSelectionListener createTreeSelectionListener() {
		return e -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jtree.getLastSelectedPathComponent();
			if (selectedNode != null) {
				// Handle different node selections
				if (selectedNode.getParent() != null && selectedNode.getParent().equals(treeDB)) {
					String selectedDBTableName = selectedNode.toString();
					if (nodeClickListener != null) {
						nodeClickListener.onNodeDBTableClicked(selectedDBTableName);
					}
				} else if (selectedNode.equals(treeFile) && nodeClickListener != null) {
					nodeClickListener.onNodeStructuredClicked();
				} else if (selectedNode.equals(treeUnstructureImage) && nodeClickListener != null) {
					nodeClickListener.onNodeImageClicked();
				} else if (selectedNode.equals(treeUnstructureImageEnc) && nodeClickListener != null) {
					nodeClickListener.onNodeEncImageClicked();
				}
			}
		};
	}

	// Initializes and returns the tree model with dynamic data
	private DefaultTreeModel initDataTree() {
		// Main tree structure
		root = new DefaultMutableTreeNode("Type");
		DefaultTreeModel treeModel = new DefaultTreeModel(root);

		// Structured nodes
		treeStructure = new DefaultMutableTreeNode("Structure");
		treeDB = new DefaultMutableTreeNode("KOALA DB");
		treeFile = new DefaultMutableTreeNode("New Structure");
		treeStructure.add(treeFile);
		treeStructure.add(treeDB);

		treeUnstructure = new DefaultMutableTreeNode("Unstructure");

		// Add nodes dynamically from the database
		try (Connection conn = KoalaDAO.makeDBConnect()) {
			Vector<String> tableNames = KoalaDAO.readDBTableName(conn);
			for (String name : tableNames) {
				treeDB.add(new DefaultMutableTreeNode(name));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "DB-Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
		}

		// Unstructured nodes
		treeUnstructureText = new DefaultMutableTreeNode("Text");
		treeUnstructureImage = new DefaultMutableTreeNode("Image");
		treeUnstructureImageEnc = new DefaultMutableTreeNode("Image-Encryption");
		treeUnstructureAudio = new DefaultMutableTreeNode("Audio");
		treeUnstructureVideo = new DefaultMutableTreeNode("Video");
		treeUnstructure.add(treeUnstructureImage);
		treeUnstructure.add(treeUnstructureImageEnc);

		root.add(treeStructure);
		root.add(treeUnstructure);

		return treeModel;
	}

	// Initializes the bottom panel with the de-identification process steps
	private void initWestPanelBottom() {
		westPanelBottom = new JPanel(new GridLayout(7, 1));
		TitledBorder metaBorder = BorderFactory.createTitledBorder("[De-identification Process]");
		metaBorder.setTitleJustification(TitledBorder.CENTER);
		westPanelBottom.setBorder(metaBorder);

		// Process steps (inactivated)
		JRadioButton rbImportData = new JRadioButton("1. Import Data");
		JRadioButton radioButton2 = new JRadioButton("2. Model Setting: Attribute Setting");
		JRadioButton radioButton4 = new JRadioButton("3. Privacy Model Selection");
		JRadioButton radioButton5 = new JRadioButton("4. Parameter Value Setting");
		JRadioButton radioButton6 = new JRadioButton("5. Utility Metric Selection");
		JRadioButton radioButton7 = new JRadioButton("6. Taxonomy Tree Building");

		// Add radio buttons to the panel
		westPanelBottom.add(rbImportData);
		westPanelBottom.add(radioButton2);
		westPanelBottom.add(radioButton4);
		westPanelBottom.add(radioButton5);
		westPanelBottom.add(radioButton6);
		westPanelBottom.add(radioButton7);
	}

	// Refreshes the JTree with updated data
	@Override
	public void onRefresh() {
		DefaultTreeModel newModel = initDataTree();
		jtree.setModel(newModel);
		// Optionally expand all nodes after refresh
		for (int i = 0; i < jtree.getRowCount(); i++) {
			jtree.expandRow(i);
		}
	}
}

