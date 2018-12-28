package org.medibloc.panacea.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

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

    /**
     * Sign a hash with the private key of this key pair.
     * @param transactionHash   the hash to sign
     * @return  An {@link ECDSASignature} of the hash
     */
    public ECDSASignature sign(byte[] transactionHash) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(this.privKey, Keys.CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(transactionHash);

        return new ECDSASignature(components[0], components[1]).toCanonicalised();
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
