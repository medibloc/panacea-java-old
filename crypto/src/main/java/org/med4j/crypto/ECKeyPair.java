package org.med4j.crypto;

import java.math.BigInteger;

public class ECKeyPair {
    private final BigInteger privKey;
    private final BigInteger pubKey;

    public ECKeyPair(BigInteger privKey, BigInteger pubKey) {
        this.privKey = privKey;
        this.pubKey = pubKey;
    }

    public BigInteger getPrivKey() {
        return privKey;
    }

    public BigInteger getPubKey() {
        return pubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ECKeyPair ecKeyPair = (ECKeyPair) o;

        if (privKey != null
                ? !privKey.equals(ecKeyPair.privKey) : ecKeyPair.privKey != null) {
            return false;
        }

        return pubKey != null
                ? pubKey.equals(ecKeyPair.pubKey) : ecKeyPair.pubKey == null;
    }

    @Override
    public int hashCode() {
        int result = privKey != null ? privKey.hashCode() : 0;
        result = 31 * result + (pubKey != null ? pubKey.hashCode() : 0);
        return result;
    }
}
