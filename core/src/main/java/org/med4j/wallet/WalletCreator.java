package org.med4j.wallet;

import org.med4j.crypto.ECKeyPair;

import java.io.File;

public class WalletCreator {
    /** Create new key pair and make a wallet file. */
    public static WalletFile create(String password) {
        return null;
    }

    /** Create new key pair and make a wallet file. */
    public static WalletFile create(String password, File destination) {
        return null;
    }

    /** Create new wallet file for the existing key pair. */
    public static WalletFile create(String password, ECKeyPair ecKeyPair) {
        return null;
    }

    public static WalletFile create(String password, File destination, ECKeyPair ecKeyPair) {
        return null;
    }
}
