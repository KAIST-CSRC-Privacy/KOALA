package kr.ac.kaist.csrc.koala.gui.structure;

import javax.swing.*;
import java.awt.*;

public class FeatureTextArea {

	JPanel panel;

	public FeatureTextArea() {
		panel = new JPanel();
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Add some margin to panel
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		//The manual for feature analysis
		String[] manualLines = {
			"Feature Analysis Instruction",
			"You can do feature analysis on whether original table or anonymized table",
			"The result column is the last feature column",
			"The value of this column consists of feature abbreviations",
			"The followings are the meaning of the abbreviations",
			"                                                   ",
			"1. AS : All-Same : The sensitive values in a equivalence class are identical",
			"2. SK : Skewness type 1 : The frequency of a certain sensitive value is much bigger than others",
			"3. SK2 : Skewness type 2 : The distance between overall sensitive attribute distribution and " +
					"an equivalence class is large",
			"4. SI : Similarity : The sensitive values in an equivalence class have similar meanings",
			"5. SE : Sensitivity : There are too many user-defined sensitive values in an equivalence class",
			"6. NR : Narrow Range : The distribution of numerical sensitive values in equivalence class is too narrow",
			"7. PR : Proximity : After removing some outliers of numerical sensitive values in equivalence class, " +
					"the distribution is too narrow",
			"8. DU : Duplicate : From the original table, the record can have multiple records in the same table. "
		};

		for (String line : manualLines) {
			JLabel label = new JLabel(line);
			label.setFont(new Font("Arial", Font.PLAIN, 16));
			label.setForeground(Color.DARK_GRAY);
			label.setAlignmentX(Component.LEFT_ALIGNMENT); // left align
			label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // add vertical spacing
			panel.add(label);
		}
	}

	public JPanel getInstruction() {
		return panel;
	}
}
