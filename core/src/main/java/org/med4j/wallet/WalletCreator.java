package org.med4j.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;

import java.io.File;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class WalletCreator {
    /**
     * Create new wallet.
     *
     * @param password required
     * @param existingKeyPair optional(if null, generate new key pair)
     * @return New Wallet
     */
    public static String create(String password, ECKeyPair existingKeyPair) throws Exception {
        validatePassword(password);

        ECKeyPair ecKeyPair;
        if (existingKeyPair == null) {
            ecKeyPair = Keys.generateKeysFromPassphrase(password);
        } else {
            ecKeyPair = existingKeyPair;
        }

        WalletFile walletFile = Wallet.createWalletFile(password, ecKeyPair);

        File destinationDirectory = getOrCreateDir(getDefaultWalletFilePath());
        String fileName = getWalletFileName(walletFile);
        File destination = new File(destinationDirectory, fileName);

        new ObjectMapper().writeValue(destination, walletFile);

        return fileName;
    }

    private static void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null.");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password can not be empty.");
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

    private static String getWalletFileName(WalletFile walletFile) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        return now.format(format) + walletFile.getAddress() + ".json";
    }
}
