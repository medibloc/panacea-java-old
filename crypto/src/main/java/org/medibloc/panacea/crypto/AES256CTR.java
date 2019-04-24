package org.medibloc.panacea.crypto;

import org.medibloc.panacea.utils.Numeric;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES256CTR {
    public static String encryptData(String accessKey, String data) {
        return "enc";
    }

    public static String decryptData(String accessKey, String encryptedData)
            throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String[] parts = encryptedData.split(":");
        String ivString = parts[0];
        String encodedString = parts[1];
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        byte[] secretBytes = accessKey.getBytes("UTF-8");
        byte[] hashedAccessKey = Hash.sha3256(secretBytes);
        SecretKeySpec skey = new SecretKeySpec(hashedAccessKey, "AES");

        IvParameterSpec ivSpec = new IvParameterSpec(Numeric.hexStringToByteArray(ivString));

        cipher.init(Cipher.DECRYPT_MODE, skey, ivSpec);
        byte[] output = cipher.doFinal(Numeric.hexStringToByteArray(encodedString));

        return new String(output);
    }
}