package org.medibloc.panacea.did;

import org.medibloc.panacea.crypto.CipherException;
import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;
import org.medibloc.panacea.key.KeyHolder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class DidKey extends KeyHolder {
    private String keyId;

    /** Default constructor is used to deserialize JSON value. */
    DidKey() { }

    public DidKey(String keyId, String password, DidKeyOption didKeyOption) throws InvalidKeySpecException, NoSuchAlgorithmException, CipherException {
        this(keyId, password, Keys.generateKeyPair(), didKeyOption);
    }

    public DidKey(String keyId, String password, ECKeyPair ecKeyPair, DidKeyOption didKeyOption) throws CipherException {
        super(password, ecKeyPair, didKeyOption);
        this.keyId = keyId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DidKey didKey = (DidKey) o;

        return keyId != null ? keyId.equals(didKey.keyId) : didKey.keyId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keyId != null ? keyId.hashCode() : 0);
        return result;
    }
}
