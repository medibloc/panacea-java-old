package org.med4j.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bouncycastle.crypto.generators.SCrypt;
import org.med4j.crypto.CipherException;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Hash;
import org.med4j.crypto.Keys;
import org.med4j.utils.Numeric;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import static org.med4j.crypto.SecureRandomUtils.secureRandom;

public class Account {
    private static final int CURRENT_VERSION = 3;

    private static final String AES_128_CTR = "aes-128-ctr";
    private static final String PBKDF2 = "pbkdf2";
    private static final String SCRYPT = "scrypt";

    private static final int N = 1 << 13; //8192
    private static final int P = 1;

    private static final int R = 8;
    private static final int DKLEN = 32;

    private static final int PRIVATE_KEY_SIZE = 32;
    private static final int PUBLIC_KEY_SIZE = 33;
    private static final int KDFPARAMS_SALT_SIZE = 32;
    private static final int IV_SIZE = 16;

    private int version;
    private String id;
    private Crypto crypto; // = encryptedPrivKey in medjs

    private String address;

    Account() { }

    Account(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws CipherException {
        setAddress(Keys.compressPubKey(ecKeyPair.getPubKey()));
        setV3Settings(accountOption);
        generateCryptoValues(password, ecKeyPair, accountOption);
    }

    public int getVersion() {
        return version;
    }

    void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    private void setV3Settings(AccountOption option) {
        setVersion(CURRENT_VERSION);

        Crypto crypto = new Crypto();
        String cipher = option.getCipher() != null ? option.getCipher() : AES_128_CTR;
        crypto.setCipher(cipher);
        String kdf = option.getKdf() != null ? option.getKdf() : SCRYPT;
        crypto.setKdf(kdf);

        CipherParams cipherParams = new CipherParams();
        crypto.setCipherparams(cipherParams);

        ScryptKdfParams kdfParams = new ScryptKdfParams();
        int dklen = option.getDklen() != -1 ? option.getDklen() : DKLEN;
        kdfParams.setDklen(dklen);
        int n = option.getN() != -1 ? option.getN() : N;
        kdfParams.setN(n);
        int p = option.getP() != -1 ? option.getP() : P;
        kdfParams.setP(p);
        int r = option.getR() != -1 ? option.getR() : R;
        kdfParams.setR(r);
        crypto.setKdfparams(kdfParams);

        setCrypto(crypto);
    }

    private void generateCryptoValues(String password, ECKeyPair ecKeyPair, AccountOption option) throws CipherException {
        if (this.version == 0 || this.crypto == null || this.crypto.kdfparams == null) {
            throw new IllegalArgumentException("Account options was not set. Valid Crypto values should be set before call generateCryptoValues() method.");
        }

        this.id = option.getUuid() != null ? option.getUuid() : UUID.randomUUID().toString();

        byte[] salt = option.getSalt() != null ? option.getSalt() : generateRandomBytes(KDFPARAMS_SALT_SIZE);
        byte[] iv = option.getIv() != null ? option.getIv() : generateRandomBytes(IV_SIZE);

        byte[] derivedKey;
        if (this.crypto.kdfparams instanceof ScryptKdfParams) {
            derivedKey = getDerivedKey(password, salt, (ScryptKdfParams)this.crypto.kdfparams);
        } else {
            throw new IllegalArgumentException("Unsupported kdf");
        }
        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] privateKeyBytes = Numeric.toBytesPadded(ecKeyPair.getPrivKey(), PRIVATE_KEY_SIZE);
        byte[] cipherText = Keys.performCipherOperation(Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);
        byte[] mac = generateMac(derivedKey, cipherText);

        this.crypto.kdfparams.setSalt(Numeric.toHexStringNoPrefix(salt));
        this.crypto.cipherparams.setIv(Numeric.toHexStringNoPrefix(iv));
        this.crypto.ciphertext = Numeric.toHexStringNoPrefix(cipherText);
        this.crypto.mac = Numeric.toHexStringNoPrefix(mac);
    }

    byte[] getDerivedKey(String password, ScryptKdfParams kdfParams) {
        return SCrypt.generate(password.getBytes(Charset.forName("UTF-8")), Numeric.hexStringToByteArray(kdfParams.getSalt()), kdfParams.getN(), kdfParams.getR(), kdfParams.getP(), kdfParams.getDklen());
    }

    byte[] getDerivedKey(String password, byte[] salt, ScryptKdfParams kdfParams) {
        return SCrypt.generate(password.getBytes(Charset.forName("UTF-8")), salt, kdfParams.getN(), kdfParams.getR(), kdfParams.getP(), kdfParams.getDklen());
    }

    static byte[] generateMac(byte[] derivedKey, byte[] cipherText) {
        byte[] result = new byte[16 + cipherText.length];

        System.arraycopy(derivedKey, 16, result, 0, 16);
        System.arraycopy(cipherText, 0, result, 16, cipherText.length);

        return Hash.sha3256(result);
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom().nextBytes(bytes);
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        Account that = (Account) o;

        if (getVersion() != that.getVersion()) {
            return false;
        }
        if (getId() != null
                ? !getId().equals(that.getId())
                : that.getId() != null) {
            return false;
        }
        if (getCrypto() != null
                ? !getCrypto().equals(that.getCrypto())
                : that.getCrypto() != null) {
            return false;
        }
        if (getAddress() != null
                ? !getAddress().equals(that.getAddress())
                : that.getAddress() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getVersion();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + (getCrypto() != null ? getCrypto().hashCode() : 0);
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }

    public static class Crypto {
        private String cipher;
        private String ciphertext;
        private CipherParams cipherparams;

        private String kdf;
        private KdfParams kdfparams;

        private String mac;

        public String getCipher() {
            return cipher;
        }

        public void setCipher(String cipher) {
            this.cipher = cipher;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public CipherParams getCipherparams() {
            return cipherparams;
        }

        public void setCipherparams(CipherParams cipherparams) {
            this.cipherparams = cipherparams;
        }

        public String getKdf() {
            return kdf;
        }

        public void setKdf(String kdf) {
            this.kdf = kdf;
        }

        public KdfParams getKdfparams() {
            return kdfparams;
        }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "kdf")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ScryptKdfParams.class, name = SCRYPT)
        })
        public void setKdfparams(KdfParams kdfparams) {
            this.kdfparams = kdfparams;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Crypto)) {
                return false;
            }

            Crypto that = (Crypto) o;

            if (getCipher() != null
                    ? !getCipher().equals(that.getCipher())
                    : that.getCipher() != null) {
                return false;
            }
            if (getCiphertext() != null
                    ? !getCiphertext().equals(that.getCiphertext())
                    : that.getCiphertext() != null) {
                return false;
            }
            if (getCipherparams() != null
                    ? !getCipherparams().equals(that.getCipherparams())
                    : that.getCipherparams() != null) {
                return false;
            }
            if (getKdf() != null
                    ? !getKdf().equals(that.getKdf())
                    : that.getKdf() != null) {
                return false;
            }
            if (getKdfparams() != null
                    ? !getKdfparams().equals(that.getKdfparams())
                    : that.getKdfparams() != null) {
                return false;
            }
            return getMac() != null
                    ? getMac().equals(that.getMac()) : that.getMac() == null;
        }

        @Override
        public int hashCode() {
            int result = getCipher() != null ? getCipher().hashCode() : 0;
            result = 31 * result + (getCiphertext() != null ? getCiphertext().hashCode() : 0);
            result = 31 * result + (getCipherparams() != null ? getCipherparams().hashCode() : 0);
            result = 31 * result + (getKdf() != null ? getKdf().hashCode() : 0);
            result = 31 * result + (getKdfparams() != null ? getKdfparams().hashCode() : 0);
            result = 31 * result + (getMac() != null ? getMac().hashCode() : 0);
            return result;
        }
    }

    public static class CipherParams {
        private String iv;

        public CipherParams() {
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CipherParams)) {
                return false;
            }

            CipherParams that = (CipherParams) o;

            return getIv() != null
                    ? getIv().equals(that.getIv()) : that.getIv() == null;
        }

        @Override
        public int hashCode() {
            int result = getIv() != null ? getIv().hashCode() : 0;
            return result;
        }

    }

    interface KdfParams {
        int getDklen();

        String getSalt();

        void setSalt(String salt);
    }

    public static class Aes128CtrKdfParams implements KdfParams {
        private int dklen;
        private int c;
        private String prf;
        private String salt;

        public Aes128CtrKdfParams() {
        }

        @Override
        public int getDklen() {
            return dklen;
        }

        public void setDklen(int dklen) {
            this.dklen = dklen;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        public String getPrf() {
            return prf;
        }

        public void setPrf(String prf) {
            this.prf = prf;
        }

        @Override
        public String getSalt() {
            return salt;
        }

        @Override
        public void setSalt(String salt) {
            this.salt = salt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Aes128CtrKdfParams)) {
                return false;
            }

            Aes128CtrKdfParams that = (Aes128CtrKdfParams) o;

            if (dklen != that.dklen) {
                return false;
            }
            if (c != that.c) {
                return false;
            }
            if (getPrf() != null
                    ? !getPrf().equals(that.getPrf())
                    : that.getPrf() != null) {
                return false;
            }
            return getSalt() != null
                    ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + c;
            result = 31 * result + (getPrf() != null ? getPrf().hashCode() : 0);
            result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
            return result;
        }
    }

    public static class ScryptKdfParams implements KdfParams {
        private int dklen;
        private int n;
        private int p;
        private int r;
        private String salt;

        public ScryptKdfParams() {
        }

        @Override
        public int getDklen() {
            return dklen;
        }

        public void setDklen(int dklen) {
            this.dklen = dklen;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }

        public int getP() {
            return p;
        }

        public void setP(int p) {
            this.p = p;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        @Override
        public String getSalt() {
            return salt;
        }

        @Override
        public void setSalt(String salt) {
            this.salt = salt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ScryptKdfParams)) {
                return false;
            }

            ScryptKdfParams that = (ScryptKdfParams) o;

            if (dklen != that.dklen) {
                return false;
            }
            if (n != that.n) {
                return false;
            }
            if (p != that.p) {
                return false;
            }
            if (r != that.r) {
                return false;
            }
            return getSalt() != null
                    ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
        }

        @Override
        public int hashCode() {
            int result = dklen;
            result = 31 * result + n;
            result = 31 * result + p;
            result = 31 * result + r;
            result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
            return result;
        }
    }
}
