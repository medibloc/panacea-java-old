package org.medibloc.panacea.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.medibloc.panacea.crypto.CipherException;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;
import org.medibloc.panacea.utils.Numeric;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class AccountUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** Create new Account. A new key pair will be generated internally. */
    public static Account createAccount(String password, AccountOption accountOption) throws Exception {
        ECKeyPair ecKeyPair = Keys.generateKeysFromPassphrase(password);
        return createAccount(password, ecKeyPair, accountOption);
    }

    /** Create new Account with the given key pair. */
    public static Account createAccount(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws Exception {
        validatePassword(password);
        Keys.validateECKeyPair(ecKeyPair);

        if (accountOption == null) accountOption = new AccountOption();

        Account account = new Account(password, ecKeyPair, accountOption);

        return account;
    }

    public static String convertAccountToJson(Account account) throws Exception {
        return objectMapper.writeValueAsString(account);
    }

    public static Account parseJsonAccount(String jsonAccount) throws Exception {
        return objectMapper.readValue(jsonAccount, Account.class);
    }

    public static File saveAccountToDefaultPath(Account account) throws Exception {
        return saveAccount(account, getDefaultAccountFilePath());
    }

    public static File saveAccount(Account account, String destinationPath) throws Exception {
        File destinationDirectory = getOrCreateDir(destinationPath);
        String fileName = getAccountFileName(account);
        File destination = new File(destinationDirectory, fileName);

        objectMapper.writeValue(destination, account);
        return destination;
    }

    public static Account loadAccount(String accountFilePath) throws Exception {
        return objectMapper.readValue(new File(accountFilePath), Account.class);
    }

    public static ECKeyPair getKeyPair(Account account, String password) throws Exception {
        Account.Crypto crypto = account.getCrypto();

        byte[] cipherText = Numeric.hexStringToByteArray(crypto.getCiphertext());
        byte[] iv = Numeric.hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] mac = Numeric.hexStringToByteArray(crypto.getMac());

        byte[] derivedKey;
        Account.KdfParams kdfParams = crypto.getKdfparams();
        if (kdfParams instanceof Account.ScryptKdfParams) {
            derivedKey = account.getDerivedKey(password, (Account.ScryptKdfParams)kdfParams);
        } else {
            throw new IllegalArgumentException("Unsupported kdf");
        }

        byte[] derivedMac = account.generateMac(derivedKey, cipherText);

        if (!Arrays.equals(derivedMac, mac)) {
            throw new CipherException("Invalid password provided");
        }

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        BigInteger privateKey = new BigInteger(Keys.decryptPrivateKey(cipherText, encryptKey, iv));
        return new ECKeyPair(privateKey, Keys.getPublicKeyFromPrivatekey(privateKey));
    }

    private static void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null.");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password can not be empty.");
        }

        // TODO - add validation
    }

    private static String getDefaultAccountFilePath() {
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
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat form = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'Z--'");
        return form.format(today) + account.getAddress() + ".json";
    }
}
