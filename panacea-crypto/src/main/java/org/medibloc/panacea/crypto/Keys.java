package org.medibloc.panacea.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.medibloc.panacea.utils.Numeric;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Keys {
    private Keys() {}

    static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
    static final BigInteger HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);

    private static int KEY_SOURCE_SIZE = 64;
    private static byte[] PBKDF2_SALT = "medibloc".getBytes();
    private static int PBKDF2_ITERATIONS = 32768;
    static int PBKDF2_KEY_SIZE = 32;

    /**
     * Generate private key which is less than 'FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551', according to secp256k1.
     */
    private static byte[] generatePrivateKeyFromPassphrase(String passphrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = Pbkdf2.pbkdf2(passphrase.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, PBKDF2_KEY_SIZE);

        // TODO : check before generating passphrase
        while (privKeyBytes[0]==-1 && privKeyBytes[1]==-1 && privKeyBytes[2]==-1 && privKeyBytes[3]==-1) {
            privKeyBytes = Pbkdf2.pbkdf2(passphrase.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, PBKDF2_KEY_SIZE);
        }

        return privKeyBytes;
    }

    /**
     * Returns public key point from the given private key.
     *
     * @param privKey the private key to derive the public key from
     * @return ECPoint public key
     */
    private static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group
         * order, but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    public static ECKeyPair getEcKeyPair(String privateKey) {
        BigInteger privKey = new BigInteger(privateKey, 16);
        BigInteger pubKey = getPublicKeyFromPrivatekey(privKey);

        return new ECKeyPair(privKey, pubKey);
    }

    public static BigInteger getPublicKeyFromPrivatekey(BigInteger privateKey) {
        ECPoint pubKeyPoint = publicPointFromPrivate(privateKey);
        byte[] encoded = pubKeyPoint.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length)); // remove prefix
    }

    public static ECKeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String keySource1 = Numeric.toHexStringNoPrefix(SecureRandomUtils.generateRandomBytes(KEY_SOURCE_SIZE/2));
        String keySource2 = Numeric.toHexStringNoPrefix(SecureRandomUtils.generateRandomBytes(KEY_SOURCE_SIZE/2));
        return generateKeyPair(keySource1 + keySource2);
    }

    public static ECKeyPair generateKeyPair(String source) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = generatePrivateKeyFromPassphrase(source);
        BigInteger privKey = new BigInteger(1, privKeyBytes);
        BigInteger pubKey = getPublicKeyFromPrivatekey(privKey);

        return new ECKeyPair(privKey, pubKey);
    }

    public static String compressPubKey(BigInteger pubKey) {
        String pubKeyYPrefix = pubKey.testBit(0) ? "03" : "02";
        String pubKeyHex = pubKey.toString(16);
        String pubKeyX = pubKeyHex.substring(0, 64);
        return pubKeyYPrefix + pubKeyX;
    }

    public static void validateECKeyPair(ECKeyPair ecKeyPair) {
        ECPoint expectedPubKeyPoint = publicPointFromPrivate(ecKeyPair.getPrivKey());
        byte[] encoded = expectedPubKeyPoint.getEncoded(false);
        BigInteger expectedPubKey = new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length)); // remove prefix

        if (expectedPubKey.compareTo(ecKeyPair.getPubKey()) != 0) {
            throw new IllegalArgumentException("The ECKeyPair is invalid.");
        } else {
            return;
        }
    }

    public static byte[] decryptPrivateKey(byte[] cipherText, byte[] encryptKey, byte[] iv) throws CipherException {
        return performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, cipherText);
    }

    public static byte[] performCipherOperation(int mode, byte[] iv, byte[] encryptKey, byte[] text) throws CipherException {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(text);
        } catch (Exception e) {
            if (e instanceof NoSuchPaddingException || e instanceof NoSuchAlgorithmException
                    || e instanceof InvalidAlgorithmParameterException || e instanceof InvalidKeyException
                    || e instanceof BadPaddingException || e instanceof IllegalBlockSizeException) {
                throw new CipherException("Error performing cipher operation", e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    static ECPoint decompressKey(String publicKey) {
        return decompressKey(new BigInteger(publicKey, 16), publicKey.startsWith("03"));
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(Keys.CURVE.getCurve()));
        compEnc[0] = (byte)(yBit ? 0x03 : 0x02);
        return Keys.CURVE.getCurve().decodePoint(compEnc);
    }

    public static String getSharedSecretKey(String myPrivateKey, String otherPublicKey) throws CipherException {
        if (myPrivateKey.length() != 64) {
            throw new CipherException("The length of private key should be 64.");
        }
        if (otherPublicKey.length() != 66) {
            throw new CipherException("The length of public key should be 66.");
        }

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(new BigInteger(myPrivateKey, 16), Keys.CURVE);
        ECPublicKeyParameters pubKey = new ECPublicKeyParameters(decompressKey(otherPublicKey), Keys.CURVE);

        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(privKey);
        return agreement.calculateAgreement(pubKey).toString(16);
    }
}
