package kr.ac.kaist.csrc.koala.utils.image;

import java.awt.image.BufferedImage;

public class ImageNoise {
    public static BufferedImage imageNoise(BufferedImage inputImg) {
        if (!CVUtility.checkOpenCV()) {
            return inputImg;
        }

        return CVUtility.makeBufferedNoiseImage(inputImg.getWidth(), inputImg.getHeight());
    }
}