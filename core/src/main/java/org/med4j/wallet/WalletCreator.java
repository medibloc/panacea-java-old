package org.med4j.wallet;

import org.med4j.crypto.ECKeyPair;
import org.med4j.crypto.Keys;

public class WalletCreator {
    /**
     * Create new wallet.
     *
     * @param password required
     * @param keyDirectory optional(if null, use default key directory)
     * @param existingKeyPair optional(if null, generate new key pair)
     * @return New Wallet
     */
    public static Wallet create(String password, String keyDirectory, ECKeyPair existingKeyPair) throws Exception {
        Wallet wallet = new Wallet();
        wallet.validatePassword(password);
        wallet.setWalletFile(new WalletFile(keyDirectory));

        ECKeyPair ecKeyPair = existingKeyPair;
        if (ecKeyPair == null) {
            ecKeyPair = Keys.generateKeysFromPassphrase(password);
        }
        wallet.setEcKeyPair(ecKeyPair);

        return wallet;
    }
}
