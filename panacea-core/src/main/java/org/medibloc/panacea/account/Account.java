package org.medibloc.panacea.account;

import org.medibloc.panacea.crypto.CipherException;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;
import org.medibloc.panacea.key.KeyHolder;

public class Account extends KeyHolder {
    private String address;

    /** Default constructor is used to deserialize JSON value. */
    Account() { }

    Account(String password, ECKeyPair ecKeyPair, AccountOption accountOption) throws CipherException {
        super(password, ecKeyPair, accountOption.getKeyStoreOption());
        setAddress(Keys.compressPubKey(ecKeyPair.getPubKey()));
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Account account = (Account) o;

        return address != null ? address.equals(account.address) : account.address == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}
