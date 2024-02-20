package kr.ac.kaist.csrc.koala.utils.image;

import java.awt.image.BufferedImage;

public class ImageScrambling {
    public static BufferedImage faceScramble(BufferedImage inputImg, int scrVal, boolean isAuto) {
        if (!CVUtility.checkOpenCV()) {
            return inputImg;
        }
        int scrambleMount;
        switch (scrVal) {
            case 1 -> scrambleMount = 10;
            case 2 -> scrambleMount = 7;
            case 3 -> scrambleMount = 5;
            case 4 -> scrambleMount = 3;
            case 5 -> scrambleMount = 2;
            default -> {
                return inputImg;
            }
        }
        return isAuto ? CVUtility.CVFaceScrambling(inputImg, scrambleMount) : CVUtility.CVScrambling(inputImg, scrambleMount);
    }
}
