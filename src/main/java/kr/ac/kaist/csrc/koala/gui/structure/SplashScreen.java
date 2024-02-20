package kr.ac.kaist.csrc.koala.gui.structure;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SplashScreen extends JWindow {

	public SplashScreen() {
		// Set up the content pane
		JPanel content = (JPanel) getContentPane();

		// Screen dimensions for centering the splash screen
		int width = 1000;
		int height = 400;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - width) / 2;
		int y = (screen.height - height) / 2;
		setBounds(x, y, width, height);

		// Load and add the image to the splash screen
		URL imagePath = getClass().getResource("/MainKoala.png");
		if (imagePath != null) {
			ImageIcon imageIcon = new ImageIcon(imagePath);
			JLabel label = new JLabel(imageIcon);
			content.add(label, BorderLayout.CENTER);
		} else {
			showErrorDialog("The splash image could not be found.");
		}

		// Finalize setup
		pack();
		setVisible(true);
		setLocationRelativeTo(null);

		// Display the splash screen for a certain amount of time
		try {
			Thread.sleep(4000); // Display time in milliseconds
		} catch (InterruptedException e) {
			showErrorDialog("The splash screen was interrupted.");
		}
		setVisible(false);
	}

	private void showErrorDialog(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}
}