package org.med4j.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bouncycastle.crypto.generators.SCrypt;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;
import org.med4j.utils.Numeric;

import javax.crypto.Cipher;
import java.security.Key;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    private static final int PUBLIC_KEY_SIZE = 66;
    private static final int KDFPARAMS_SALT_SIZE = 32;
    private static final int IV_SIZE = 16;

    private int version;
    private String id;
    private Crypto crypto; // = encryptedPrivKey in medjs

    private String address;

    /**
     * Create Account class with given Crypto class.
     * If
     */
    public Account(Crypto crypto) {
        this.crypto = crypto;
    }

    public Account(String password, ECKeyPair ecKeyPair) {
        setV3Settings();
        setAddress(Numeric.toHexStringZeroPadded(ecKeyPair.getPubKey(), PUBLIC_KEY_SIZE));
        generateCryptoValues(password, ecKeyPair);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Set Crypto properties V3.
     */
    public void setV3Settings() {
        this.version = CURRENT_VERSION;

        Crypto crypto = new Crypto();
        crypto.cipher = AES_128_CTR;
        crypto.kdf = SCRYPT;

        ScryptKdfParams kdfParams = new ScryptKdfParams();
        kdfParams.setDklen(DKLEN);
        kdfParams.setN(N);
        kdfParams.setP(P);
        kdfParams.setR(R);
        crypto.setKdfparams(kdfParams);

        this.crypto = crypto;
    }

    public void generateCryptoValues(String password, ECKeyPair ecKeyPair) {
        if (this.version == 0 || this.crypto == null || this.crypto.kdfparams == null) {
            throw new IllegalArgumentException("Account options was not set. Create AccountOption class with 'useDefaultSettings=true' parameter.");
        }

        this.id = UUID.randomUUID().toString();

        this.crypto.kdfparams.setSalt(generateRandomBytes(KDFPARAMS_SALT_SIZE));
        this.crypto.cipherparams.setIv(generateRandomBytes(IV_SIZE));

        byte[] derivedKey = SCrypt.generate(password.getBytes(UTF_8), salt, N, R, P, DKLEN);
        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] privateKeyBytes = Numeric.toBytesPadded(ecKeyPair.getPrivKey(), Keys.PRIVATE_KEY_SIZE);
        byte[] cipherText = performCipherOperation(Cipher.ENCRYPT_MODE, iv, encryptKey, privateKeyBytes);
        byte[] mac = generateMac(derivedKey, cipherText);

        WalletFile walletFile = new WalletFile();
        walletFile.setAddress(Keys.getAddress(ecKeyPair));

        WalletFile.Crypto crypto = new WalletFile.Crypto();
        crypto.setCipher(CIPHER);
        crypto.setCiphertext(Numeric.toHexStringNoPrefix(cipherText));

        WalletFile.CipherParams cipherParams = new WalletFile.CipherParams();
        crypto.setCipherparams(cipherParams);

        crypto.setMac(Numeric.toHexStringNoPrefix(mac));
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom().nextBytes(bytes);
        return bytes;
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
                @JsonSubTypes.Type(value = Aes128CtrKdfParams.class, name = AES_128_CTR),
                @JsonSubTypes.Type(value = ScryptKdfParams.class, name = SCRYPT)
        })
        // To support my Ether Wallet keys uncomment this annotation & comment out the above
        //  @JsonDeserialize(using = KdfParamsDeserialiser.class)
        // Also add the following to the ObjectMapperFactory
        // objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
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
