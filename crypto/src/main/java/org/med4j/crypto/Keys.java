package org.med4j.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.med4j.crypto.SecureRandomUtils.secureRandom;

public class Keys {
    private Keys() {}

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    private static byte[] PBKDF2_SALT = "medibloc".getBytes();
    private static int PBKDF2_ITERATIONS = 32768;
    static int PBKDF2_KEY_SIZE = 32;

    /**
     * Create a keypair using SECP-256k1 curve.
     *
     * <p>Private keypairs are encoded using PKCS8
     *
     * <p>Private keys are encoded using X.509
     */
    private static KeyPair createSecp256k1KeyPair() throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecGenParameterSpec, secureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    static KeyPair generateKeys() throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        return createSecp256k1KeyPair();
    }

    /**
     * Generate private key which is less than 'FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551', according to secp256k1.
     */
    private static byte[] generatePrivateKeyFromPassphrase(String passphrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = Pbkdf2.pbkdf2(passphrase.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, PBKDF2_KEY_SIZE);

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

    public static ECKeyPair generateKeysFromPassphrase(String passphrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privKeyBytes = generatePrivateKeyFromPassphrase(passphrase);
        BigInteger privKey = new BigInteger(1, privKeyBytes);

        ECPoint pubKeyPoint = publicPointFromPrivate(privKey);
        byte[] encoded = pubKeyPoint.getEncoded(true);
        BigInteger pubKey = new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length)); // remove prefix

        return new ECKeyPair(privKey, pubKey);
    }

    public static String compressPubKey(BigInteger pubKey) {
        String pubKeyYPrefix = pubKey.testBit(0) ? "03" : "02";
        String pubKeyHex = pubKey.toString(16);
        String pubKeyX = pubKeyHex.substring(0, 64);
        return pubKeyYPrefix + pubKeyX;
    }
}
