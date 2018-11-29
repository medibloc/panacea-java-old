package org.med4j.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;
import sun.jvm.hotspot.oops.CompiledICHolder;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AccountUtils {
    /**
     * Create new Account.
     *
     * @param password required
     * @param existingKeyPair optional(if null, generate new key pair)
     * @param accountOption optional(if null, use default option)
     * @return New Wallet
     */
    public static Account createAccount(String password, ECKeyPair existingKeyPair, AccountOption accountOption) throws Exception {
        validatePassword(password);

        ECKeyPair ecKeyPair;
        if (existingKeyPair == null) {
            ecKeyPair = Keys.generateKeysFromPassphrase(password);
        } else {
            ecKeyPair = existingKeyPair;
        }

        AccountOption option;
        if (accountOption == null) {
            option = AccountOption.getDefaultOption();
        } else {
            option = accountOption;
        }

        return new Account(password, ecKeyPair, option);
    }

    public static File saveAccount(Account account) throws Exception {
        return saveAccount(account, getDefaultWalletFilePath());
    }

    public static File saveAccount(Account account, String destinationPath) throws Exception {
        File destinationDirectory = getOrCreateDir(destinationPath);
        String fileName = getAccountFileName(account);
        File destination = new File(destinationDirectory, fileName);

        new ObjectMapper().writeValue(destination, account);
        return destination;
    }

    private static void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null.");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password can not be empty.");
        }
    }

    private static String getDefaultWalletFilePath() {
        String lowerOsName = System.getProperty("os.name").toLowerCase();

        if (lowerOsName.startsWith("mac")) {
            return String.format(
                    "%s%sLibrary%sMedibloc", System.getProperty("user.home"), File.separator,
                    File.separator);
        } else if (lowerOsName.startsWith("win")) {
            return String.format("%s%sMedibloc", System.getenv("APPDATA"), File.separator);
        } else {
            // TODO - test on android
            return String.format("%s%s.Medibloc", System.getProperty("user.home"), File.separator);
        }
    }

    private static File getOrCreateDir(String destinationDir) {
        File destination = new File(destinationDir);

        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new RuntimeException("Unable to create destination directory [" + destinationDir + "]");
            }
        }

        return destination;
    }

    private static String getAccountFileName(Account account) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return now.format(format) + account.getAddress() + ".json";
    }
}

class Crypto {
    static final String AES_128_CTR = "pbkdf2";
    static final String SCRYPT = "scrypt";

    private String cipher;
    private String ciphertext;
    private CipherParams cipherparams;

    private String kdf;
    private KdfParams kdfparams;

    private String mac;

    public Crypto(String cipher, String ciphertext, CipherParams cipherParams, String kdf, KdfParams kdfParams, String mac) {

    }

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

class CipherParams {
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
}

class Aes128CtrKdfParams implements KdfParams {
    private int dklen;
    private int c;
    private String prf;
    private String salt;

    public Aes128CtrKdfParams() {
    }

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

    public String getSalt() {
        return salt;
    }

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

class ScryptKdfParams implements KdfParams {
    private int dklen;
    private int n;
    private int p;
    private int r;
    private String salt;

    public ScryptKdfParams() {
    }

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

    public String getSalt() {
        return salt;
    }

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
