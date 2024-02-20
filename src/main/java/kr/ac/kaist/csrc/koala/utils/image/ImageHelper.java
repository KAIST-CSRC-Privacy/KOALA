package kr.ac.kaist.csrc.koala.utils.image;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageHelper {

    public static byte[] readFileAsBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static JPanel createTabPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    public static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(listener);
        return button;
    }

    public static void resizeImageIconToPanelSize(JLabel label, JTabbedPane panel) {
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image originalImage = imageIcon.getImage();

            int panelWidth = panel.getWidth();
            int panelHeight = panel.getHeight();

            int imageWidth = originalImage.getWidth(null);
            int imageHeight = originalImage.getHeight(null);

            if (imageWidth <= panelWidth && imageHeight <= panelHeight) {
                return;
            }

            double aspectRatio = (double) imageWidth / imageHeight;
            int newWidth, newHeight;
            double panelAspectRatio = (double) panelWidth / panelHeight;
            if (panelAspectRatio > aspectRatio) {
                newHeight = panelHeight;
                newWidth = (int) (newHeight * aspectRatio);
            } else {
                newWidth = panelWidth;
                newHeight = (int) (newWidth / aspectRatio);
            }
            Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(resizedImage));
        }
    }

    public static void resizeImageIconToHalfPanelHeight(JLabel label, JTabbedPane panel) {
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            Image originalImage = imageIcon.getImage();

            int maxHeight = panel.getHeight() / 2;

            int originalWidth = originalImage.getWidth(null);
            int originalHeight = originalImage.getHeight(null);
            double aspectRatio = (double) originalWidth / originalHeight;

            int newHeight = Math.min(originalHeight, maxHeight);
            int newWidth = (int) (newHeight * aspectRatio);

            Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(resizedImage));
        }
    }


}
