package kr.ac.kaist.csrc.koala.gui.unstructure;

import kr.ac.kaist.csrc.koala.utils.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImagePanel extends JTabbedPane {

	public JTabbedPane imagePane;
	public JPanel imageEastPanel;
	public JPanel imageSouthPanel;
	public JMenuBar imageMenuBar;
	private final ButtonGroup radioGroup = new ButtonGroup();
	private final JLabel originalImageLabel = new JLabel();
	private final JLabel anonymizedImageLabel = new JLabel();
	private final JLabel compareOriginalLabel = new JLabel();
	private final JLabel compareAnonymizedLabel = new JLabel();
	private JSlider slider;
	private JCheckBox faceDetectChk;
	private JRadioButton blurRadio;
	private JRadioButton maskRadio;
	private JRadioButton pixelRadio;
	private JRadioButton scbRadio;
	private JButton saveButton;
	private BufferedImage originalImage;
	private BufferedImage anonymizedImage;
	private String originalImageFormat;

	private enum AnonymizationType {
		BLUR, MASK, PIXEL, SCRAM
	}

	public ImagePanel() {
		initImagePanel();
		initImageEastPanel();
		initImageSouthPanel();
		initImageMenuBar();
	}

	private void initImagePanel() {
		imagePane = new JTabbedPane();
		// Tab 1: Original Image
		JPanel orgTab = ImageHelper.createTabPanel();
		orgTab.setLayout(new BoxLayout(orgTab, BoxLayout.Y_AXIS));
		originalImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		orgTab.add(originalImageLabel);
		imagePane.addTab("Original image", orgTab);

		// Tab 2: Anonymized Image
		JPanel resultTab = ImageHelper.createTabPanel();
		resultTab.add(anonymizedImageLabel);
		anonymizedImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		imagePane.addTab("Anonymized image", resultTab);

		// Tab 3: Compare Image
		JPanel tab3 = ImageHelper.createTabPanel();
		tab3.setLayout(new BoxLayout(tab3, BoxLayout.Y_AXIS));
		// Before
		JPanel originalPanel = new JPanel();
		originalPanel.setLayout(new BoxLayout(originalPanel, BoxLayout.Y_AXIS));
		JLabel beforeLabel = new JLabel("Before");
		beforeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		compareOriginalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		originalPanel.add(beforeLabel);
		originalPanel.add(compareOriginalLabel);
		// After
		JPanel anonymizedPanel = new JPanel();
		anonymizedPanel.setLayout(new BoxLayout(anonymizedPanel, BoxLayout.Y_AXIS));
		JLabel afterLabel = new JLabel("After");
		afterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		compareAnonymizedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		anonymizedPanel.add(afterLabel);
		anonymizedPanel.add(compareAnonymizedLabel);

		tab3.add(originalPanel);
		tab3.add(anonymizedPanel);
		imagePane.addTab("Compare image", tab3);

		imagePane.setEnabledAt(1, false);
		imagePane.setEnabledAt(2, false);
	}

	private void initImageEastPanel() {
		imageEastPanel = new JPanel();
		imageEastPanel.setLayout(new BoxLayout(imageEastPanel, BoxLayout.Y_AXIS));

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

		ActionListener radioListener = e -> updateSouthPanel(Objects.requireNonNull(getSelectedAnonymizationType()));

		Component spacer = Box.createRigidArea(new Dimension(10, 30));

		blurRadio = new JRadioButton("Blurring");
		blurRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
		blurRadio.addActionListener(radioListener);

		maskRadio = new JRadioButton("Masking");
		maskRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
		maskRadio.addActionListener(radioListener);

		pixelRadio = new JRadioButton("Pixelation");
		pixelRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
		pixelRadio.addActionListener(radioListener);

		scbRadio = new JRadioButton("Scrambling");
		scbRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
		scbRadio.addActionListener(radioListener);

		faceDetectChk = new JCheckBox("Face Recognition");
		faceDetectChk.setAlignmentX(Component.CENTER_ALIGNMENT);
		faceDetectChk.addActionListener(e -> {
			if (!CVUtility.checkOpenCV()) {
				faceDetectChk.setSelected(false);
			}
		});

		saveButton = ImageHelper.createButton("Save Result Image", e -> saveAnonymizedImage(originalImageFormat));
		saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		saveButton.setEnabled(false);

		radioGroup.add(blurRadio);
		radioGroup.add(maskRadio);
		radioGroup.add(pixelRadio);
		radioGroup.add(scbRadio);

		radioPanel.add(blurRadio);
		radioPanel.add(maskRadio);
		radioPanel.add(pixelRadio);
		radioPanel.add(scbRadio);

		imageEastPanel.add(Box.createVerticalStrut(50));
		imageEastPanel.add(faceDetectChk);
		imageEastPanel.add(spacer);
		imageEastPanel.add(radioPanel);
		imageEastPanel.add(spacer);
		imageEastPanel.add(saveButton);
	}

	private void updateSouthPanel(AnonymizationType type) {
		imageSouthPanel.removeAll();
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> anonymizeImage(type));
		switch (type) {
			case BLUR, PIXEL, SCRAM -> {
				final int[] setVal = {0};
				slider = new JSlider(JSlider.HORIZONTAL, 0, 5, setVal[0]);
				slider.setMajorTickSpacing(1);
				slider.setPaintTicks(true);
				slider.setPaintLabels(true);
				slider.addChangeListener(e -> setVal[0] = slider.getValue());
				imageSouthPanel.add(slider);
				imageSouthPanel.add(applyButton);
			}
			case MASK -> imageSouthPanel.add(applyButton);
		}

		imageSouthPanel.revalidate();
		imageSouthPanel.repaint();
	}

	private AnonymizationType getSelectedAnonymizationType() {
		if (blurRadio.isSelected()) {
			return AnonymizationType.BLUR;
		}
		if (maskRadio.isSelected()) {
			return AnonymizationType.MASK;
		}
		if (pixelRadio.isSelected()) {
			return AnonymizationType.PIXEL;
		}
		if (scbRadio.isSelected()) {
			return AnonymizationType.SCRAM;
		}
		return null; // No checkbox selected
	}

	private void initImageSouthPanel() {
		imageSouthPanel = new JPanel();
	}

	private void initImageMenuBar() {
		imageMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem imageLoad = new JMenuItem("Load Image");
		imageLoad.addActionListener(this::loadImageAction);
		imageMenuBar.add(fileMenu);
		fileMenu.add(imageLoad);
	}

	private void loadImageAction(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser("./data/");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
			"Image Files", ImageIO.getReaderFileSuffixes());
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				originalImage = ImageIO.read(selectedFile);
				originalImageLabel.setIcon(new ImageIcon(originalImage));
				ImageHelper.resizeImageIconToPanelSize(originalImageLabel, imagePane);
				imagePane.setEnabledAt(1, true);
				imagePane.setSelectedIndex(0);
				saveButton.setEnabled(false);
				String fileName = selectedFile.getName();
				int dotIndex = fileName.lastIndexOf('.');
				if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
					originalImageFormat = fileName.substring(dotIndex + 1).toLowerCase();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Failed to load the image.", "File Open Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void saveAnonymizedImage(String originalFormat) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
			"Image files(" + originalFormat + ")", originalFormat);
		fileChooser.setFileFilter(filter);

		int userSelection = fileChooser.showSaveDialog(null);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String filePath = fileToSave.getAbsolutePath();

			if (!filePath.toLowerCase().endsWith("." + originalFormat)) {
				filePath += "." + originalFormat;
			}

			try {
				ImageIO.write(anonymizedImage, originalFormat, new File(filePath));
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Failed to Save the image.", "File Save Error",
												JOptionPane.ERROR_MESSAGE);

			}
		}
	}

	private void anonymizeImage(AnonymizationType type) {
		if (originalImage == null) {
			return;
		}
		BufferedImage imageToAnonymize = ImageHelper.deepCopy(originalImage);
		anonymizedImage = null;
		anonymizedImage = switch (type) {
			case BLUR -> ImageBlurring.faceBlur(imageToAnonymize, getSliderValue(), getCheckAuto());
			case MASK -> ImageMasking.faceMasking(imageToAnonymize, getCheckAuto());
			case PIXEL -> ImagePixelation.facePixelation(imageToAnonymize, getSliderValue(), getCheckAuto());
			case SCRAM -> ImageScrambling.faceScramble(imageToAnonymize, getSliderValue(), getCheckAuto());
		};

		if (anonymizedImage != null) {
			anonymizedImageLabel.setIcon(new ImageIcon(anonymizedImage));
			ImageHelper.resizeImageIconToPanelSize(anonymizedImageLabel, imagePane);
			compareOriginalLabel.setIcon(new ImageIcon(originalImage));
			ImageHelper.resizeImageIconToHalfPanelHeight(compareOriginalLabel, imagePane);
			compareAnonymizedLabel.setIcon(new ImageIcon(anonymizedImage));
			ImageHelper.resizeImageIconToHalfPanelHeight(compareAnonymizedLabel, imagePane);
			imagePane.setEnabledAt(2, true);
			imagePane.setSelectedIndex(1); // Switch to the second tab
		}
		saveButton.setEnabled(true);
	}


	private int getSliderValue() {
		return slider.getValue();
	}

	private boolean getCheckAuto() {
		return faceDetectChk.isSelected();
	}

}