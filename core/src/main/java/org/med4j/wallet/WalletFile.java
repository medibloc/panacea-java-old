package org.med4j.wallet;

import java.io.File;

public class WalletFile {
    File keyDirectory;

    public WalletFile(String keyDirectoryPath) {
        if (keyDirectoryPath == null || keyDirectoryPath.isEmpty()) {
            this.keyDirectory = getOrCreateDir(getDefaultKeyDirectory());
        } else {
            this.keyDirectory = getOrCreateDir(keyDirectoryPath);
        }
    }

    private String getDefaultKeyDirectory() {
        String lowerOsName = System.getProperty("os.name").toLowerCase();

        if (lowerOsName.startsWith("mac")) {
            return String.format(
                    "%s%sLibrary%sEthereum", System.getProperty("user.home"), File.separator,
                    File.separator);
        } else if (lowerOsName.startsWith("win")) {
            return String.format("%s%sEthereum", System.getenv("APPDATA"), File.separator);
        } else {
            // TODO - test on android
            return String.format("%s%s.ethereum", System.getProperty("user.home"), File.separator);
        }
    }

    private File getOrCreateDir(String destinationDir) {
        File destination = new File(destinationDir);

        if (!destination.exists()) {
            if (!destination.mkdirs()) {
                throw new RuntimeException("Unable to create destination directory [" + destinationDir + "]");
            }
        }

        return destination;
    }
}
