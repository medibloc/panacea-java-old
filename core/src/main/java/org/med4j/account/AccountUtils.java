package org.med4j.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AccountUtils {

    /** Create new Account. A new key pair will be generated internally. */
    public static Account createAccount(String password, AccountOption accountOption) throws Exception {
        ECKeyPair ecKeyPair = Keys.generateKeysFromPassphrase(password);
        return createAccount(password, ecKeyPair, accountOption);
    }

    /** Create new Account with the given key pair. */
    public static Account createAccount(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws Exception {
        validatePassword(password);

        if (accountOption == null) accountOption = new AccountOption();

        Account account = new Account(password, ecKeyPair, accountOption);

        return account;
    }

    public static File saveAccountToDefaultPath(Account account) throws Exception {
        return saveAccount(account, getDefaultAccountFilePath());
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
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return now.format(format) + account.getAddress() + ".json";
    }
}
