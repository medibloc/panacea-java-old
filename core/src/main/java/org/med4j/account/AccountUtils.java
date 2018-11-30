package org.med4j.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;

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
     * @return New Wallet
     */
    public static Account createAccount(String password, ECKeyPair existingKeyPair) throws Exception {
        validatePassword(password);

        ECKeyPair ecKeyPair;
        if (existingKeyPair == null) {
            ecKeyPair = Keys.generateKeysFromPassphrase(password);
        } else {
            ecKeyPair = existingKeyPair;
        }

        return new Account(password, ecKeyPair);
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
