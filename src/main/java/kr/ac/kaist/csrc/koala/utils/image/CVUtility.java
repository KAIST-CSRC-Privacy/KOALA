package kr.ac.kaist.csrc.koala.utils.image;

import kr.ac.kaist.csrc.koala.Main;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CVUtility {

    private static CascadeClassifier faceXml;
    // Method to check OpenCV library
    public static boolean checkOpenCV() {
        try {
            nu.pattern.OpenCV.loadLocally();
            InputStream is = Main.class.getResourceAsStream("/haarcascades/haarcascade_frontalface_alt.xml");
            if (is == null) {
                throw new IllegalArgumentException("Cannot find resource ");
            }
            Path tempFile = Files.createTempFile("haarcascade", ".xml");
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            faceXml = new CascadeClassifier(tempFile.toString());
            tempFile.toFile().deleteOnExit();
            return true;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Cannot load the native code library: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Cannot load the Open CV", "Library Load Error", JOptionPane.WARNING_MESSAGE);
            return false;
        } catch (IOException e) {
            System.err.println("Cannot find resource: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Cannot load resource", "Library Load Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Method for image blurring
    public static BufferedImage CVBlur(BufferedImage inputImg, int blurVal) {
        Mat image = bufferedImageToMat(inputImg);
        Imgproc.GaussianBlur(image, image, new Size(blurVal, blurVal), 0);
        return matToBufferedImage(image);
    }

    // Method for face blurring
    public static BufferedImage CVFaceBlur(BufferedImage inputImg, int blurVal) {
        Mat image = bufferedImageToMat(inputImg);
        CascadeClassifier faceDetector = faceXml;
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        Rect[] facesArray = faceDetections.toArray();
        if (facesArray.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Failed to recognize face.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return inputImg;
        } else {
            for (Rect rect : facesArray) {
                System.out.println("Detected face: x = " + rect.x + ", y = " + rect.y + ", width = " + rect.width + ", height = " + rect.height);
                Mat faceArea = image.submat(rect);
                Imgproc.GaussianBlur(faceArea, faceArea, new Size(blurVal, blurVal), 0);
            }
            return matToBufferedImage(image);
        }
    }

    // Method for face masking
    public static BufferedImage CVFaceMasking(BufferedImage inputImg) {
        Mat image = bufferedImageToMat(inputImg);
        CascadeClassifier faceDetector = faceXml;
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        Rect[] facesArray = faceDetections.toArray();
        if (facesArray.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Failed to recognize face.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return inputImg;
        } else {
            for (Rect rect : facesArray) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 0), -1);
            }
            return matToBufferedImage(image);
        }
    }

    // Method for image pixelation
    public static BufferedImage CVPixelation(BufferedImage inputImg, int pixelVal) {
        Mat image = bufferedImageToMat(inputImg);
        Mat pixelated = new Mat();
        Imgproc.resize(image, pixelated, new Size(), 1.0 / pixelVal, 1.0 / pixelVal, Imgproc.INTER_LINEAR);
        Imgproc.resize(pixelated, pixelated, image.size(), 0, 0, Imgproc.INTER_NEAREST);
        pixelated.copyTo(image);
        return matToBufferedImage(image);
    }

    // Method for face pixelation
    public static BufferedImage CVFacePixelation(BufferedImage inputImg, int pixelVal) {
        Mat image = bufferedImageToMat(inputImg);
        CascadeClassifier faceDetector = faceXml;
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        Rect[] facesArray = faceDetections.toArray();
        if (facesArray.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Failed to recognize face.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return inputImg;
        } else {
            for (Rect rect : facesArray) {
                image = facePixelate(image, rect, pixelVal);
            }
            return matToBufferedImage(image);
        }
    }

    // Method for image scrambling
    public static BufferedImage CVScrambling(BufferedImage inputImg, int blockSize) {
        Mat image = bufferedImageToMat(inputImg);
        return matToBufferedImage(scrambleRegion(image, new Rect(0, 0, image.width(), image.height()), blockSize));
    }

    // Method for face scrambling
    public static BufferedImage CVFaceScrambling(BufferedImage inputImg, int scrVal) {
        Mat image = bufferedImageToMat(inputImg);
        CascadeClassifier faceDetector = faceXml;
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        Rect[] facesArray = faceDetections.toArray();
        if (facesArray.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Failed to recognize face.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return inputImg;
        } else {
            for (Rect rect : facesArray) {
                image = scrambleRegion(image, rect, scrVal);
            }
            return matToBufferedImage(image);
        }
    }

    public static BufferedImage makeBufferedNoiseImage(int width, int height) {
        Mat noiseImage = new Mat(new Size(width, height), CvType.CV_8UC3);
        Core.randu(noiseImage, 0, 255);
        return matToBufferedImage(noiseImage);
    }

    // Converts OpenCV Mat to BufferedImage
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // Get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    // Converts BufferedImage to OpenCV Mat
    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat facePixelate(Mat image, Rect area, int pixelSize) {
        Mat faceArea = new Mat(image, area);
        Mat pixelated = new Mat();
        Imgproc.resize(faceArea, pixelated, new Size(), 1.0 / pixelSize, 1.0 / pixelSize, Imgproc.INTER_LINEAR);
        Imgproc.resize(pixelated, pixelated, faceArea.size(), 0, 0, Imgproc.INTER_NEAREST);
        pixelated.copyTo(faceArea);
        return image;
    }

    // Scrambles a specified region of an image
    private static Mat scrambleRegion(Mat image, Rect area, int blockSize) {
        List<Mat> blocks = new ArrayList<>();
        int blocksX = area.width / blockSize;
        int blocksY = area.height / blockSize;
        for (int i = 0; i < blocksY; i++) {
            for (int j = 0; j < blocksX; j++) {
                int x = j * blockSize + area.x;
                int y = i * blockSize + area.y;
                Rect blockRect = new Rect(x, y, blockSize, blockSize);
                blocks.add(new Mat(image, blockRect));
            }
        }
        Collections.shuffle(blocks);
        int count = 0;
        for (int i = 0; i < blocksY; i++) {
            for (int j = 0; j < blocksX; j++) {
                int x = j * blockSize + area.x;
                int y = i * blockSize + area.y;
                Mat block = blocks.get(count++);
                block.copyTo(image.submat(new Rect(x, y, blockSize, blockSize)));
            }
        }
        return image;
    }
}

