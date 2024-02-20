package kr.ac.kaist.csrc.koala;

import kr.ac.kaist.csrc.koala.gui.listener.NodeClickListener;
import kr.ac.kaist.csrc.koala.gui.structure.Structured;
import kr.ac.kaist.csrc.koala.gui.structure.WestPanel;
import kr.ac.kaist.csrc.koala.gui.unstructure.ImageEncryptionPanel;
import kr.ac.kaist.csrc.koala.gui.unstructure.ImagePanel;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Mainframe extends JFrame {

	public static JTabbedPane centerPane;
	public static JPanel eastPanel;
	public static JPanel southPanel;
	public static JMenuBar jMenuBar;
	private WestPanel westPanel;
	private Structured mainStructure = new Structured(null);
	private ImagePanel imagePanel = new ImagePanel();
	private ImageEncryptionPanel imageEncPanel = new ImageEncryptionPanel();


	public Mainframe() {
		// Icon Image Setting
		setBounds(300, 300, 1000, 1000);
		URL iconUrl = getClass().getResource("/koala.png");
		if (iconUrl != null) {
			Image mainToolkitIcon = Toolkit.getDefaultToolkit().getImage(iconUrl);
			setIconImage(mainToolkitIcon);
		} else {
			System.err.println("Icon resource not found");
		}

		// Panel Initial Setting
		westPanel = new WestPanel();
		centerPane = new JTabbedPane();
		eastPanel = new JPanel();
		southPanel = new JPanel();
		jMenuBar = new JMenuBar();

		westPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		centerPane.setBorder(BorderFactory.createLineBorder(Color.black));
		eastPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		southPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		jMenuBar.setBorder(BorderFactory.createLineBorder(Color.black));

		setLayout(new BorderLayout());
		add(westPanel, BorderLayout.WEST);
		add(centerPane, BorderLayout.CENTER);
		add(eastPanel, BorderLayout.EAST);
		add(southPanel, BorderLayout.SOUTH);
		add(jMenuBar, BorderLayout.NORTH);

		westPanel.setPreferredSize(new Dimension(200, this.getHeight()));
		eastPanel.setPreferredSize(new Dimension(200, this.getHeight()));

		westPanel.setNodeClickListener(new NodeClickListener() {
			@Override
			public void onNodeStructuredClicked() {
				if (checkContents()) {
					mainStructure.koalaInput = null;
					mainStructure = new Structured(westPanel);
					mainStructure.initLoadFileMenuBar();
					panelConstruction(mainStructure.centerPane, mainStructure.eastPanel, mainStructure.southPanel,
						mainStructure.menuBar);
				}
			}

			@Override
			public void onNodeDBTableClicked(String dbName) {
				if (checkContents()) {
					mainStructure.koalaInput = null;
					mainStructure = new Structured(westPanel, dbName);
					mainStructure.initDBMenuBar();
					panelConstruction(mainStructure.centerPane, mainStructure.eastPanel, mainStructure.southPanel,
						mainStructure.menuBar);
				}
			}

			@Override
			public void onNodeImageClicked() {
				if (checkContents()) {
					mainStructure.koalaInput = null;
					panelConstruction(imagePanel.imagePane, imagePanel.imageEastPanel, imagePanel.imageSouthPanel,
						imagePanel.imageMenuBar);
				}
			}

			@Override
			public void onNodeEncImageClicked() {
				if (checkContents()) {
					mainStructure.koalaInput = null;
					panelConstruction(imageEncPanel.encImagePane, imageEncPanel.encImageEastPanel,
						imageEncPanel.encImageSouthPanel, imageEncPanel.encImageMenuBar);
				}
			}
		});

		//frame setting
		setTitle("KOALA - Kaist Opensource Anonymization pLAtform");
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private boolean checkContents() {
		if (mainStructure.koalaInput == null) {
			return true;
		}

		int response = JOptionPane.showConfirmDialog(null,
			"Current work will be reset. Do you want to continue?",
			"Warning",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		return response == JOptionPane.YES_OPTION;
	}

	public void panelConstruction(JTabbedPane centerPane, JPanel eastPanel, JPanel southPanel, JMenuBar northMenu) {
		getContentPane().remove(Mainframe.centerPane);
		getContentPane().remove(Mainframe.eastPanel);
		getContentPane().remove(Mainframe.southPanel);
		getContentPane().remove(jMenuBar);

		Mainframe.centerPane = centerPane;
		Mainframe.eastPanel = eastPanel;
		Mainframe.southPanel = southPanel;
		jMenuBar = northMenu;

		add(Mainframe.centerPane, BorderLayout.CENTER);
		add(Mainframe.eastPanel, BorderLayout.EAST);
		add(Mainframe.southPanel, BorderLayout.SOUTH);
		add(jMenuBar, BorderLayout.NORTH);

		getContentPane().revalidate();
		getContentPane().repaint();
	}
}
