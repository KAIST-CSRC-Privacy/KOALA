package kr.ac.kaist.csrc.koala.gui.unstructure;

import kr.ac.kaist.csrc.koala.utils.image.ImageEncryption;
import kr.ac.kaist.csrc.koala.utils.image.ImageHelper;
import kr.ac.kaist.csrc.koala.utils.image.ImageNoise;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageEncryptionPanel {

	public JTabbedPane encImagePane;
	public JPanel encImageEastPanel;
	public JPanel encImageSouthPanel;
	public JMenuBar encImageMenuBar;
	private final JLabel encryptionImageLabel = new JLabel();
	private final JLabel decryptionImageLabel = new JLabel();
	private BufferedImage originalImage;
	private BufferedImage decryptedImage;
	private byte[] encryptedImageByte;
	private byte[] loadEncryptedByte;
	private JButton saveKencButton;
	private JButton saveImgButton;
	private JButton encryptButton;
	private JButton decryptButton;
	private String originalImageFormat;
	private String currentImageName;
	private String currentKencName;

	public ImageEncryptionPanel() {
		initEncImagePanel();
		initEncImageEastPanel();
		initEncImageSouthPanel();
		initEncImageMenuBar();
	}

	private void initEncImagePanel() {
		encImagePane = new JTabbedPane();
		// Tab 1: Image Encryption
		JPanel encryptTab = ImageHelper.createTabPanel();
		encryptTab.setLayout(new BoxLayout(encryptTab, BoxLayout.Y_AXIS));
		encryptionImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		encryptTab.add(encryptionImageLabel);
		encImagePane.addTab("Image Encryption", encryptTab);

		// Tab 2: Image Decryption
		JPanel decryptTab = ImageHelper.createTabPanel();
		decryptTab.setLayout(new BoxLayout(decryptTab, BoxLayout.Y_AXIS));
		decryptionImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		decryptTab.add(decryptionImageLabel);
		encImagePane.addTab("Image Decryption", decryptTab);

		encImagePane.addChangeListener(e -> {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                encImageEastPanel.removeAll();
                onEncryptionTabSelected();
                encImageEastPanel.revalidate();
                encImageEastPanel.repaint();
            } else if (selectedIndex == 1) {
                encImageEastPanel.removeAll();
                onDecryptionTabSelected();
                encImageEastPanel.revalidate();
                encImageEastPanel.repaint();
            }
        });

	}

	private void initEncImageEastPanel() {
		encImageEastPanel = new JPanel();
		encImageEastPanel.setLayout(new BoxLayout(encImageEastPanel, BoxLayout.Y_AXIS));
		onEncryptionTabSelected();
	}

	private JLabel getImageInfoLabel() {
		JLabel fileInfo = new JLabel(currentImageName);
		fileInfo.setFont(new Font("Arial", Font.BOLD, 18));
		fileInfo.setForeground(new Color(0, 102, 204));
		Border lineBorder = BorderFactory.createLineBorder(new Color(153, 204, 255), 2);
		Border marginBorder = new EmptyBorder(10, 20, 10, 20);
		fileInfo.setBorder(BorderFactory.createCompoundBorder(lineBorder, marginBorder));
		fileInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		return fileInfo;
	}

	private JLabel getKencInfoLabel() {
		JLabel fileInfo = new JLabel(currentKencName);
		fileInfo.setFont(new Font("Arial", Font.BOLD, 18));
		fileInfo.setForeground(new Color(213, 52, 3));
		Border lineBorder = BorderFactory.createLineBorder(new Color(255, 189, 153), 2);
		Border marginBorder = new EmptyBorder(10, 20, 10, 20);
		fileInfo.setBorder(BorderFactory.createCompoundBorder(lineBorder, marginBorder));
		fileInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		return fileInfo;
	}


	private void onEncryptionTabSelected() {
		encryptButton = ImageHelper.createButton("Encrypt Image", e -> {
			try {
				encryptButtonAction();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		saveKencButton = ImageHelper.createButton("Save Result Encrypted Image", e -> saveKEncFile());
		encryptButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		if (currentImageName == null) {
			encryptButton.setEnabled(false);
		}
		saveKencButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		if (encryptedImageByte == null) {
			saveKencButton.setEnabled(false);
		}
		encImageEastPanel.add(Box.createVerticalStrut(50));
		if (currentImageName != null) {
			encImageEastPanel.add(getImageInfoLabel());
		}
		encImageEastPanel.add(Box.createVerticalStrut(30));
		encImageEastPanel.add(encryptButton);
		encImageEastPanel.add(saveKencButton);
	}

	private void onDecryptionTabSelected() {
		decryptButton = ImageHelper.createButton("Decrypt Image", e -> {
			try {
				decryptButtonAction();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		saveImgButton = ImageHelper.createButton("Save Result Image", e -> saveDecryptedImage());

		decryptButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		if (currentKencName == null) {
			decryptButton.setEnabled(false);
		}
		saveImgButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		if (decryptedImage == null) {
			saveImgButton.setEnabled(false);
		}
		encImageEastPanel.add(Box.createVerticalStrut(50));
		if (currentKencName != null) {
			encImageEastPanel.add(getKencInfoLabel());
		}
		encImageEastPanel.add(Box.createVerticalStrut(30));
		encImageEastPanel.add(decryptButton);
		encImageEastPanel.add(saveImgButton);
	}

	private void initEncImageSouthPanel() {
		encImageSouthPanel = new JPanel();
	}

	private void initEncImageMenuBar() {
		encImageMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem imageLoad = new JMenuItem("Load Image");
		JMenuItem encImgLoad = new JMenuItem("Load Koala Encrypted Image");
		imageLoad.addActionListener(this::loadImageAction);
		encImgLoad.addActionListener(this::loadKencAction);
		encImageMenuBar.add(fileMenu);
		fileMenu.add(imageLoad);
		fileMenu.add(encImgLoad);
	}

	private void updateImgLoad() {
		encryptedImageByte = null;
		encImagePane.setEnabledAt(1, true);
		encImagePane.setSelectedIndex(0);
		encImageEastPanel.removeAll();
		onEncryptionTabSelected();
		encImageEastPanel.revalidate();
		encImageEastPanel.repaint();
		encryptButton.setEnabled(true);
	}

	private void updateKencLoad() {
		decryptionImageLabel.setIcon(null);
		decryptedImage = null;
		encImageEastPanel.removeAll();
		onDecryptionTabSelected();
		encImageEastPanel.revalidate();
		encImageEastPanel.repaint();
		encImagePane.setSelectedIndex(1);
		decryptButton.setEnabled(true);
	}

	private void encryptButtonAction() throws Exception {
		JPasswordField pwdField = new JPasswordField(15);
		Object[] message = {
			"Enter password for encryption:", pwdField
		};

		int option = JOptionPane.showConfirmDialog(null, message, "Password Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			char[] input = pwdField.getPassword();
			if (input.length <= 15) {
				encryptProcess(new String(input));
			} else {
				JOptionPane.showMessageDialog(null, "Password must be up to 15 characters long.");
			}
		}
	}

	private void decryptButtonAction() {
		JPasswordField pwdField = new JPasswordField(15);
		Object[] message = {
			"Enter password for encryption:", pwdField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Password Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			char[] input = pwdField.getPassword();
			decryptProcess(new String(input));
		}
	}

	private void encryptProcess(String password) throws Exception {
		encryptedImageByte = ImageEncryption.encryptImageToByte(originalImage, originalImageFormat, password);
		if (originalImage == null) {
			return;
		}
		BufferedImage imageToAnonymize = ImageHelper.deepCopy(originalImage);
		BufferedImage anonymizedImage = ImageNoise.imageNoise(imageToAnonymize);
		if (anonymizedImage != null) {
			encryptionImageLabel.setIcon(new ImageIcon(anonymizedImage));
		}
		saveKencButton.setEnabled(true);
	}

	private void decryptProcess(String password) {
		try {
			decryptedImage = ImageEncryption.decryptImage(loadEncryptedByte, password);
			if (decryptedImage == null) {
				throw new IllegalArgumentException("Decryption failed.");
			}
			decryptionImageLabel.setIcon(new ImageIcon(decryptedImage));
			ImageHelper.resizeImageIconToPanelSize(decryptionImageLabel, encImagePane);
			saveImgButton.setEnabled(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Incorrect file or password.", "Decryption Error",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	private void loadImageAction(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser("./data/");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				originalImage = ImageIO.read(selectedFile);
				encryptionImageLabel.setIcon(new ImageIcon(originalImage));
				ImageHelper.resizeImageIconToPanelSize(encryptionImageLabel, encImagePane);
				encImagePane.setEnabledAt(1, true);
				encImagePane.setSelectedIndex(0);
				currentImageName = selectedFile.getName();
				int dotIndex = currentImageName.lastIndexOf('.');
				if (dotIndex > 0 && dotIndex < currentImageName.length() - 1) {
					originalImageFormat = currentImageName.substring(dotIndex + 1).toLowerCase();
				}
				updateImgLoad();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Failed to load the image.", "File Open Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void loadKencAction(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser("./data/");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Koala Encrypted File(.kenc)", "kenc");
		fileChooser.setFileFilter(filter);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				byte[] loadByte = ImageHelper.readFileAsBytes(selectedFile);
				if (!ImageEncryption.checkKoalaFlag(loadByte)) {
					JOptionPane.showMessageDialog(null, "Invalid Koala encrypted file format.", "File Format Error",
													JOptionPane.ERROR_MESSAGE);
					return;
				}
				loadEncryptedByte = loadByte;
				currentKencName = selectedFile.getName();
				updateKencLoad();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Incorrect or corrupted .kenc file.", "File Open Error",
												JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void saveKEncFile() {
		JFileChooser fileChooser = new JFileChooser("./data/");
		fileChooser.setDialogTitle("Specify a file to save");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Koala Encrypted File(.kenc)", "kenc");
		fileChooser.setFileFilter(filter);
		int userSelection = fileChooser.showSaveDialog(null);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String filePath = fileToSave.getAbsolutePath();
			if (!filePath.toLowerCase().endsWith(".kenc")) {
				filePath += ".kenc";
			}
			try (FileOutputStream fos = new FileOutputStream(filePath)) {
				fos.write(encryptedImageByte);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Failed to save the encrypted file.", "Save Error",
												JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void saveDecryptedImage() {
		JFileChooser fileChooser = new JFileChooser("./data/");
		fileChooser.setDialogTitle("Specify a file to save");
		// File chooser filter setup omitted for brevity
		int userSelection = fileChooser.showSaveDialog(null);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			String filePath = fileToSave.getAbsolutePath();
			// File extension and save logic omitted for brevity
			try {
				// Assume decryptedImage and extension are correctly defined elsewhere
				ImageIO.write(decryptedImage, "png", new File(filePath)); // Simplified for the example
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Failed to save the decrypted image.", "Save Error",
												JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
