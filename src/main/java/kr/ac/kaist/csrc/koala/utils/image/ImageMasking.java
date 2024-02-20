package kr.ac.kaist.csrc.koala.utils.image;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.*;

public class ImageMasking {
    private static BufferedImage resultImage;

    public static BufferedImage faceMasking(BufferedImage inputImg, boolean isAuto) {
        if (!isAuto) return maskingSetting(inputImg);
        if (!CVUtility.checkOpenCV()) return inputImg;
        return CVUtility.CVFaceMasking(inputImg);
    }

    public static BufferedImage maskingSetting(BufferedImage imageToAnonymize) {
        resultImage = imageToAnonymize;
        JFrame frame = new JFrame("Masking Settings");
        frame.setLayout(new BorderLayout());
        JLabel imageLabel = new JLabel(new ImageIcon(imageToAnonymize));
        frame.add(imageLabel, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel();
        JTextField widthField = new JTextField(5);
        JTextField heightField = new JTextField(5);
        JButton applyButton = new JButton("Apply");

        int[] clickedCoordinates = new int[2];
        for (ActionListener al : applyButton.getActionListeners()) {
            applyButton.removeActionListener(al);
        }
        JOptionPane.showMessageDialog(frame, "Click on the location to mask!");
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickedCoordinates[0] = e.getX();
                clickedCoordinates[1] = e.getY();
                inputPanel.setVisible(true);
                frame.pack();
                imageLabel.setIcon(new ImageIcon(ImageMasking.drawDimmedImage(imageToAnonymize)));
                imageLabel.removeMouseListener(this);
            }
        });

        applyButton.addActionListener(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());
                if (width <= 0 || height <= 0) {
                    JOptionPane.showMessageDialog(frame, "Width and height should be positive values.", "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                    return;
                }
                resultImage = ImageMasking.mask(imageToAnonymize, clickedCoordinates[0], clickedCoordinates[1], width, height);
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers for width and height.", "Error",
                                                JOptionPane.ERROR_MESSAGE);
            }
        });

        inputPanel.add(new JLabel("Width:"));
        inputPanel.add(widthField);
        inputPanel.add(new JLabel("Height:"));
        inputPanel.add(heightField);
        inputPanel.add(applyButton);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.pack();
        inputPanel.setVisible(false);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        return resultImage;
    }

    public static BufferedImage drawDimmedImage(BufferedImage image) {
        BufferedImage dimmedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dimmedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.setColor(new Color(0, 0, 0, 127));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        return dimmedImage;
    }

    public static BufferedImage mask(BufferedImage image, int x, int y, int width, int height) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        int adjustedX = x - (width / 2);
        int adjustedY = y - (height / 2);
        g.fillRect(adjustedX, adjustedY, width, height);
        g.dispose();
        return image;
    }
}