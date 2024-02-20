package kr.ac.kaist.csrc.koala.utils.image;
import java.awt.image.*;

public class ImageBlurring {
    public static BufferedImage faceBlur(BufferedImage inputImg, int blurVal, boolean isAuto) {
        if (!CVUtility.checkOpenCV()) {
            return inputImg;
        }

        int blurAmount;
        switch (blurVal) {
            case 1 -> blurAmount = 7;
            case 2 -> blurAmount = 15;
            case 3 -> blurAmount = 31;
            case 4 -> blurAmount = 45;
            case 5 -> blurAmount = 75;
            default -> {
                return inputImg;
            }
        }
        return isAuto ? CVUtility.CVFaceBlur(inputImg, blurAmount) : CVUtility.CVBlur(inputImg, blurAmount);
    }
}
