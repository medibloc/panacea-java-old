package org.med4j.account;

import org.bouncycastle.crypto.generators.SCrypt;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;
import org.med4j.utils.Numeric;

import javax.crypto.Cipher;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.med4j.crypto.SecureRandomUtils.secureRandom;

public class Account {
    private static final int N = 1 << 18;
    private static final int P = 1;

    private static final int R = 8;
    private static final int DKLEN = 32;

    private static final int CURRENT_VERSION = 3;
    private static final String CIPHER = "aes-128-ctr";

    static final String AES_128_CTR = "pbkdf2";
    static final String SCRYPT = "scrypt";

    public Account(String password, ECKeyPair ecKeyPair, AccountOption accountOption) {
        byte[] salt = generateRandomBytes(32);
        byte[] derivedKey = SCrypt.generate(password.getBytes(UTF_8), salt, N, R, P, DKLEN);

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] iv = generateRandomBytes(16);

        byte[] privateKeyBytes = Numeric.toBytesPadded(ecKeyPair.getPrivKey(), Keys.PRIVATE_KEY_SIZE);

        byte[] cipherText = performCipherOperation(Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);

        byte[] mac = generateMac(derivedKey, cipherText);

        WalletFile walletFile = new WalletFile();
        walletFile.setAddress(Keys.getAddress(ecKeyPair));

        WalletFile.Crypto crypto = new WalletFile.Crypto();
        crypto.setCipher(CIPHER);
        crypto.setCiphertext(Numeric.toHexStringNoPrefix(cipherText));

        WalletFile.CipherParams cipherParams = new WalletFile.CipherParams();
        cipherParams.setIv(Numeric.toHexStringNoPrefix(iv));
        crypto.setCipherparams(cipherParams);

        crypto.setKdf(SCRYPT);
        WalletFile.ScryptKdfParams kdfParams = new WalletFile.ScryptKdfParams();
        kdfParams.setDklen(DKLEN);
        kdfParams.setN(n);
        kdfParams.setP(p);
        kdfParams.setR(R);
        kdfParams.setSalt(Numeric.toHexStringNoPrefix(salt));
        crypto.setKdfparams(kdfParams);

        crypto.setMac(Numeric.toHexStringNoPrefix(mac));
        walletFile.setCrypto(crypto);
        walletFile.setId(UUID.randomUUID().toString());
        walletFile.setVersion(CURRENT_VERSION);

        return walletFile;
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom().nextBytes(bytes);
        return bytes;
    }
}
