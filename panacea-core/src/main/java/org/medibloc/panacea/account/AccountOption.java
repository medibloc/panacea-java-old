package org.medibloc.panacea.account;

import org.medibloc.panacea.key.KeyHolderOption;

public class AccountOption {
    private KeyHolderOption keyHolderOption;

    public KeyHolderOption getKeyStoreOption() {
        return keyHolderOption;
    }

    public void setKeyStoreOption(KeyHolderOption keyHolderOption) {
        this.keyHolderOption = keyHolderOption;
    }
}
