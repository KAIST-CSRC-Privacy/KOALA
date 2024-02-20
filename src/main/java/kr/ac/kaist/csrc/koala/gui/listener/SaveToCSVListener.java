package kr.ac.kaist.csrc.koala.gui.listener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveToCSVListener implements ActionListener {

	private String[][] data;

	// Constructor that takes the data to be saved as a parameter
	public SaveToCSVListener(String[][] data) {
		this.data = data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Create a file chooser dialog
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save as CSV");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		// Filter to only show files with '.csv' extension
		fileChooser.addChoosableFileFilter(
			new javax.swing.filechooser.FileNameExtensionFilter("CSV file (*.csv)", "csv"));

		// Display the dialog and process the user's action
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// Append ".csv" extension if not present
			if (!file.getName().toLowerCase().endsWith(".csv")) {
				file = new File(file.getParentFile(), file.getName() + ".csv");
			}
			// Save the data to the selected file
			saveDataToCSV(file, data);
		}
	}

	// Writes the provided data to the given file in CSV format
	private void saveDataToCSV(File file, String[][] data) {
		try (FileWriter fw = new FileWriter(file)) {
			for (String[] row : data) {
				fw.write(String.join(",", row));
				fw.write("\n"); // New line after each row
			}
		} catch (IOException ex) {
			// Display an error message if something goes wrong during file writing
			JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
