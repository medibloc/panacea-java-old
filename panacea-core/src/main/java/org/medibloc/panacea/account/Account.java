package org.medibloc.panacea.account;

import org.medibloc.panacea.crypto.CipherException;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;
import org.medibloc.panacea.keystore.KeyStore;

public class Account {
    private KeyStore keyStore;
    private String address;

    /** Default constructor is used to deserialize JSON value. */
    Account() { }

    Account(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws CipherException {
        setKeyStore(new KeyStore(password, ecKeyPair, accountOption.getKeyStoreOption()));
        setAddress(Keys.compressPubKey(ecKeyPair.getPubKey()));
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        Account that = (Account) o;

        if (getKeyStore() != null
                ? !getKeyStore().equals(that.getKeyStore())
                : that.getKeyStore() != null) {
            return false;
        }

        if (getAddress() != null
                ? !getAddress().equals(that.getAddress())
                : that.getAddress() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (getKeyStore() != null ? getKeyStore().hashCode() : 0);
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }
}
