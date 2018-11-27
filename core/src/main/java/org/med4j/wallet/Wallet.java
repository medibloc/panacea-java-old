package org.med4j.wallet;

import org.med4j.crypto.ECKeyPair;

public class Wallet {
    private WalletFile walletFile;
    private ECKeyPair ecKeyPair;

    public WalletFile getWalletFile() {
        return walletFile;
    }

    public void setWalletFile(WalletFile walletFile) {
        this.walletFile = walletFile;
    }

    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

    public void setEcKeyPair(ECKeyPair ecKeyPair) {
        this.ecKeyPair = ecKeyPair;
    }

    public void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null.");
        }

        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password can not be empty.");
        }
    }
}
