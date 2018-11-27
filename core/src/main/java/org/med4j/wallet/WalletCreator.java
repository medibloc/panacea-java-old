package org.med4j.wallet;

import org.med4j.crypto.ECKeyPair;

public class WalletCreator {
    /** Create new key pair and make a wallet file. */
    public static Wallet create(String password) {

        create(password, null, null);
        return null;
    }

    /** Create new key pair and make a wallet file. */
    public static Wallet create(String password, String keyDirectory) {
        create(password, keyDirectory, null);
        return null;
    }

    /** Create new wallet file for the existing key pair. */
    public static Wallet create(String password, ECKeyPair ecKeyPair) {
        create(password, null, ecKeyPair);
        return null;
    }

    public static Wallet create(String password, String keyDirectory, ECKeyPair ecKeyPair) {
        Wallet wallet = new Wallet();
        wallet.validatePassword(password);
        wallet.setWalletFile(new WalletFile(keyDirectory));

        return null;
    }
}
