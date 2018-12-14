package org.med4j.crypto;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class Hash {
    private Hash() {}
    /**
     * Generates SHA3-256 digest for the given {@code input}.
     *
     * @param input The input to digest
     * @return The hash value for the given input
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static byte[] sha3256(byte[] input) {
        SHA3.Digest256 digest256 = new SHA3.Digest256();
        return digest256.digest(input);
    }
}
