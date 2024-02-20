package kr.ac.kaist.csrc.koala.utils.image;

import java.awt.image.BufferedImage;

public class ImagePixelation {
    public static BufferedImage facePixelation(BufferedImage inputImg, int pixelVal, boolean isAuto) {
        if (!CVUtility.checkOpenCV()) {
            return inputImg;
        }
        int pixelMount;
        switch (pixelVal) {
            case 1 -> pixelMount = 5;
            case 2 -> pixelMount = 10;
            case 3 -> pixelMount = 15;
            case 4 -> pixelMount = 20;
            case 5 -> pixelMount = 30;
            default -> {
                return inputImg;
            }
        }
        return isAuto ? CVUtility.CVFacePixelation(inputImg, pixelMount) : CVUtility.CVPixelation(inputImg, pixelMount);
    }
}