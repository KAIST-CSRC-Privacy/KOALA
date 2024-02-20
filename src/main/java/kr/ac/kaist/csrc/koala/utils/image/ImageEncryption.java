package kr.ac.kaist.csrc.koala.utils.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.security.MessageDigest;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class ImageEncryption {
    private static final String ALGORITHM = "AES";
    private static final byte[] FLAG = "KAIST_Koala".getBytes();

    public static byte[] encryptImageToByte(BufferedImage image, String format, String password) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();

        // flag + Image data
        byte[] dataWithFlag = new byte[FLAG.length + imageBytes.length];
        System.arraycopy(FLAG, 0, dataWithFlag, 0, FLAG.length);
        System.arraycopy(imageBytes, 0, dataWithFlag, FLAG.length, imageBytes.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = getKeyFromPassword(password);
        System.out.println(secretKey);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(dataWithFlag);
    }

    public static BufferedImage decryptImage(byte[] encryptedData, String password) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = getKeyFromPassword(password);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Check for the flag in the beginning of the decrypted data
        if (checkKoalaFlag(decryptedData)) {
            // Remove the flag from the decrypted data
            byte[] imageData = Arrays.copyOfRange(decryptedData, FLAG.length, decryptedData.length);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            return ImageIO.read(bais); // Convert the remaining data back to an image
        } else {
            throw new IllegalArgumentException("Decryption failed or wrong flag");
        }
    }

    public static boolean checkKoalaFlag(byte[] data) {
        if (data.length < FLAG.length) {
            return false;
        }
        // Check match the FLAG
        for (int i = 0; i < FLAG.length; i++) {
            if (data[i] != FLAG[i]) {
                return false;
            }
        }
        return true;
    }

    public static SecretKeySpec getKeyFromPassword(String password) throws Exception {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, ALGORITHM);
    }
}